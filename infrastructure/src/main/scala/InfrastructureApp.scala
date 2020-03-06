import software.amazon.awscdk.core.{Environment, StackProps, App => CdkApp}

object InfrastructureApp extends CdkApp with App {
  val app = new CdkApp()

  new InfrastructureStack(
    app,
    "cavegen",
    Some(StackProps.builder.env(Environment.builder.account("560736504899").region("eu-west-1").build).build)
  )

  app.synth()
}
