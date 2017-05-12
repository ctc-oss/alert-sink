organization in ThisBuild := "com.ctc.big"
version in ThisBuild := "0.1"

scalaVersion in ThisBuild := "2.11.8"

lazy val `alert-sink` =
  (project in file("."))
  .aggregate(
    `alert-sink-api`,
    `alert-sink-impl`
  )
lazy val `alert-sink-api` =
  (project in file("alert-sink-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
lazy val `alert-sink-impl` =
  (project in file("alert-sink-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`alert-sink-api`)
val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test
