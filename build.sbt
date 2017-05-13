organization in ThisBuild := "com.ctc.big"
version in ThisBuild := "0.1"

scalaVersion in ThisBuild := "2.11.8"

lazy val `alert-sink` =
  project.in(file("."))
  .aggregate(
    `alert-sink-api`,
    `alert-sink-impl`
  )
lazy val `alert-sink-api` =
  project.in(file("alert-sink-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
lazy val `alert-sink-impl` =
  project.in(file("alert-sink-impl"))
  .dependsOn(`alert-sink-api`)
  .settings(lagomForkedTestSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslBroker,
      lagomScaladslTestKit,
      macwire,
      playJsonDerivedCodecs,
      scalaTest
    )
  )
  .enablePlugins(LagomScala)

lazy val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test
lazy val playJsonDerivedCodecs = "org.julienrf" %% "play-json-derived-codecs" % "3.3"
