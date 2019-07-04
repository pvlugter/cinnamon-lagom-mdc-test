organization in ThisBuild := "com.lightbend.sample"
version in ThisBuild := "1.0-SNAPSHOT"
scalaVersion in ThisBuild := "2.12.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.3" % Provided
val scalatest = "org.scalatest" %% "scalatest" % "3.0.8" % Test

lazy val apiSettings = Seq(
  libraryDependencies += lagomScaladslApi
)

lazy val serviceSettings = Seq(
  libraryDependencies ++= Seq(
    lagomScaladslKafkaBroker,
    lagomScaladslPersistenceCassandra,
    macwire,
    scalatest,
    Cinnamon.library.cinnamonSlf4jMdc
  ),
  cinnamon in test := true,
  Test / run / javaOptions += "-Dconfig.resource=local-application.conf"
)

lazy val lagomTest = project
  .in(file("."))
  .aggregate(
    `first-api`, first,
    `second-api`, second,
    `third-api`, third
  )

lazy val `first-api` = project
  .in(file("first-api"))
  .settings(apiSettings)

lazy val first = project
  .in(file("first"))
  .enablePlugins(LagomScala, Cinnamon)
  .settings(serviceSettings)
  .dependsOn(`first-api`)

lazy val `second-api` = project
  .in(file("second-api"))
  .settings(apiSettings)

lazy val second = project
  .in(file("second"))
  .enablePlugins(LagomScala, Cinnamon)
  .settings(serviceSettings)
  //.settings(Test / run / javaOptions += "-Dcinnamon.mdc.debug.stacktraces=true")
  .dependsOn(`second-api`, `first-api`)

lazy val `third-api` = project
  .in(file("third-api"))
  .settings(apiSettings)

lazy val third = project
  .in(file("third"))
  .enablePlugins(LagomScala, Cinnamon)
  .settings(serviceSettings)
  .dependsOn(`third-api`, `second-api`)
