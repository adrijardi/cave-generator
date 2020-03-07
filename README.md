# Cave generator

Technology demonstration of a cave generation tool using ScalaJS and ThreeJS as a 3D engine.

The goal is to generate an statically hosted ScalaJS application which can randomly generate a 
cave system simulation, then apply a marching squares algorithm to generate a more organic mesh.

This application can be access at http://cavegen.coding42.com

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

Then visit `web/target/web/public/main/index-dev.html` to browse the main page in development mode.

## Facade
The facade used is the one created by [Anton Kulaga](https://github.com/antonkulaga/threejs-facade) with some 
updates to handle a newer version of three.js plus making it compatible with the webpack bundle format. 