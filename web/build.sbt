enablePlugins(SbtWeb)

enablePlugins(SbtSassify)

//enablePlugins(ScalaJSBundlerPlugin)
enablePlugins(WebScalaJSBundlerPlugin)

enablePlugins(BuildInfoPlugin)

pipelineStages in Assets += scalaJSPipeline
//pipelineStages := Seq(digest, gzip),

libraryDependencies ++= Seq(
  "org.webjars" % "font-awesome" % "5.12.0"
)

//npmDependencies in Compile += "three" -> "0.114.0"
