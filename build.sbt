lazy val js = project

lazy val web = project.settings(
  scalaJSProjects += js,
)
