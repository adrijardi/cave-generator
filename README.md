# Cave generator

## Directory Structure

 * web/
   The sbt-web project for web assets
   
 * js/
   The Scala.js project to create JS output used in the sbt-web project.

## Build

Run the following command:

``` shell
sbt assets
```

Then visit `web/target/web/public/main/index.html` to browse the main page.

#### Development build

By default, the `assets` command builds Scala.js in release mode. To build this project in development mode

``` shell
sbt "set web / scalaJSPipeline / isDevMode := true" assets
```

Then visit `web/target/web/public/main/devMod.html` to browse the main page in development mode.

### Test

Run the following command:

``` shell
sbt test
```

Then the examples in Scaladoc comments will be ran by ScalaTest.
