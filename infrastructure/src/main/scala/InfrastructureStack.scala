import software.amazon.awscdk.core._
import software.amazon.awscdk.services.codebuild.{BuildEnvironment, ComputeType, LinuxBuildImage, PipelineProject}
import software.amazon.awscdk.services.codepipeline.actions.{CodeBuildAction, GitHubSourceAction, S3DeployAction}
import software.amazon.awscdk.services.codepipeline.{Artifact, IAction, Pipeline, StageProps}
import software.amazon.awscdk.services.iam.{PolicyDocument, PolicyStatement, Role, ServicePrincipal}
import software.amazon.awscdk.services.route53._
import software.amazon.awscdk.services.s3.{Bucket, BucketAccessControl}

import scala.collection.JavaConverters._

class InfrastructureStack(parent: Construct, id: String, props: Option[StackProps])
    extends Stack(parent, id, props.orNull) {

  val bucketName   = "cavegen.coding42.com"
  val pipelineName = "cavegen-pipeline"
  val githubToken  = SecretValue.secretsManager("GitHubToken")

  val webBucket = Bucket.Builder
    .create(this, "Web bucket")
    .bucketName(bucketName)
    .versioned(true)
    .websiteIndexDocument("index.html")
    .build

  val dns = RecordSet.Builder
    .create(this, "cavegen")
    .recordType(RecordType.A)
    .recordName(bucketName)
    .zone(HostedZone.fromLookup(this, "zone", HostedZoneProviderProps.builder().domainName("coding42.com").build)) //Z27AD23N8EGKKH
    .target(                                                                                                       // TODO improve
      RecordTarget.fromAlias(
        (_: IRecordSet) =>
          new AliasRecordTargetConfig {
            override def getDnsName: String      = "s3-website-eu-west-1.amazonaws.com"
            override def getHostedZoneId: String = "Z1BKCTXD74EZPE"
          }
      )
    )
    .build

  val artifactBucket = Bucket.Builder
    .create(this, "Artifact bucket")
    .versioned(true)
    .build

  val buildRole = Role.Builder
    .create(this, "Build role")
    .assumedBy(ServicePrincipal.Builder.create("codebuild.amazonaws.com").build)
    .inlinePolicies(
      Map(
        "CodebuildAccess" -> PolicyDocument.Builder
          .create()
          .statements(
            List(
              PolicyStatement.Builder
                .create()
                .actions(List("logs:CreateLogGroup", "logs:CreateLogStream", "logs:PutLogEvents").asJava)
                .resources(List("*").asJava)
                .build,
              PolicyStatement.Builder
                .create()
                .actions(List("*").asJava)
                .resources(List(artifactBucket.getBucketArn).asJava)
                .build,
            ).asJava
          )
          .build
      ).asJava
    )
    .build

  val buildProject = PipelineProject.Builder
    .create(this, "Build project")
    .projectName("cave-generator-build")
    .environment(
      BuildEnvironment
        .builder()
        .computeType(ComputeType.SMALL)
        .buildImage(LinuxBuildImage.STANDARD_3_0)
        .build
    )
    .role(buildRole)
    .timeout(Duration.minutes(10))
    .build

  val pipelineRole = Role.Builder
    .create(this, "Pipeline role")
    .assumedBy(ServicePrincipal.Builder.create("codepipeline.amazonaws.com").build)
    .inlinePolicies(
      Map(
        "CodePipelineAccess" -> PolicyDocument.Builder
          .create()
          .statements(
            List(
              PolicyStatement.Builder
                .create()
                .actions(List("codebuild:StartBuild", "codebuild:BatchGetBuilds").asJava)
                .resources(List(buildProject.getProjectArn).asJava)
                .build,
              PolicyStatement.Builder
                .create()
                .actions(List("s3:PutObject", "s3:PutObjectAcl", "s3:PutObjectVersionAcl").asJava)
                .resources(List(webBucket.getBucketArn).asJava)
                .build,
              PolicyStatement.Builder
                .create()
                .actions(List("*").asJava)
                .resources(List(artifactBucket.getBucketArn).asJava)
                .build,
            ).asJava
          )
          .build
      ).asJava
    )
    .build

  val buildPipeline = Pipeline.Builder
    .create(this, "Build pipeline")
    .pipelineName(pipelineName)
    .artifactBucket(artifactBucket)
    .role(pipelineRole)
    .stages(
      List(
        StageProps
          .builder()
          .stageName("GitHubCheckout")
          .actions(
            List[IAction](
              GitHubSourceAction.Builder
                .create()
                .actionName("Source")
                .owner("adrijardi")
                .repo("cave-generator")
                .branch("master")
                .oauthToken(githubToken)
                .output(Artifact.artifact("source"))
                .build
            ).asJava
          )
          .build,
        StageProps
          .builder()
          .stageName("Build")
          .actions(
            List[IAction](
              CodeBuildAction.Builder
                .create()
                .actionName("Build")
                .input(Artifact.artifact("source"))
                .outputs(List(Artifact.artifact("built")).asJava)
                .project(buildProject)
                .build
            ).asJava
          )
          .build,
        StageProps
          .builder()
          .stageName("Deploy")
          .actions(
            List[IAction](
              S3DeployAction.Builder
                .create()
                .actionName("Deploy")
                .input(Artifact.artifact("built"))
                .bucket(webBucket)
                .extract(true)
                .accessControl(BucketAccessControl.PUBLIC_READ)
                .build
            ).asJava
          )
          .build
      ).asJava
    )
    .build

}
