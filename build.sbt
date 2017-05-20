organization in ThisBuild := "com.ctc.ava"

//git.useGitDescribe in ThisBuild := true
version in ThisBuild := "0.1-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.11"

scalacOptions in ThisBuild ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",

  "-feature",
  "-unchecked",
  "-deprecation",

  "-language:postfixOps",
  "-language:implicitConversions",

  "-Ywarn-unused-import",
  "-Xfatal-warnings",
  "-Xlint:_"
)

lazy val `alert-sink` =
  project.in(file("."))
  .aggregate(
    `alert-sink-api`,
    `alert-sink-impl`
  )
  .enablePlugins(GitVersioning, NoPublish)

lazy val `alert-sink-api` =
  project.in(file("alert-sink-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .enablePlugins(GitVersioning, ArtifactoryPublish)

lazy val `alert-sink-impl` =
  project.in(file("alert-sink-impl"))
  .dependsOn(`alert-sink-api`)
  .settings(lagomForkedTestSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      playJsonDerivedCodecs,
      scalaTest
    )
  )
  .settings(dockerSettings: _*)
  .enablePlugins(LagomScala, DockerPlugin, GitVersioning, NoPublish)

lazy val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test
lazy val playJsonDerivedCodecs = "org.julienrf" %% "play-json-derived-codecs" % "3.3"

lazy val dockerSettings = Seq(
  packageName in Docker := "alert-sink",
  dockerExposedPorts := Seq(8080),
  dockerRepository := Some("docker.ctc.com/big"),
  dockerBaseImage := "davidcaste/debian-oracle-java:jdk8",
  version in Docker := version.value.replaceFirst("""-SNAPSHOT""", ""),
  dockerUpdateLatest := true
)
