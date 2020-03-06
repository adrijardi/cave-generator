val sdkVersion = "1.27.0"

libraryDependencies ++= Seq(
  "software.amazon.awscdk" % "core"                 % sdkVersion,
  "software.amazon.awscdk" % "s3"                   % sdkVersion,
  "software.amazon.awscdk" % "route53"              % sdkVersion,
  "software.amazon.awscdk" % "codebuild"            % sdkVersion,
  "software.amazon.awscdk" % "codepipeline"         % sdkVersion,
  "software.amazon.awscdk" % "codepipeline-actions" % sdkVersion,
  "software.amazon.awscdk" % "iam"                  % sdkVersion,
  "junit"                  % "junit"                % "4.12" % Test,
)
