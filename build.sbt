organization in ThisBuild := "com.ctc.ava"

git.useGitDescribe in ThisBuild := true

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
  .settings(commonSettings: _*)
  .enablePlugins(GitVersioning)

lazy val `alert-sink-api` =
  project.in(file("alert-sink-api"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      scalaTest
    )
  )
  .enablePlugins(GitVersioning, ArtifactoryPlugin)

lazy val `alert-sink-impl` =
  project.in(file("alert-sink-impl"))
  .dependsOn(`alert-sink-api`)
  .settings(lagomForkedTestSettings: _*)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceJdbc,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      playJsonDerivedCodecs,
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
      "mysql" % "mysql-connector-java" % "6.0.5",
      scalaTest
    )
  )
  .settings(dockerSettings: _*)
  .enablePlugins(LagomScala, OpenShiftPlugin, GitVersioning)

lazy val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test
lazy val playJsonDerivedCodecs = "org.julienrf" %% "play-json-derived-codecs" % "3.3"

lazy val commonSettings = Seq(
  headerLicense := Some(HeaderLicense.Custom(
    "©Concurrent Technologies Corporation 2017"
  ))
)

lazy val dockerSettings = Seq(
  packageName in Docker := "alert-sink",
  version in Docker := "latest",
  dockerExposedPorts := Seq(9000),
  dockerRepository := Some("docker.ctc.com/big")
)

lagomKafkaEnabled in ThisBuild := false
lagomKafkaAddress in ThisBuild := "172.17.0.1:9092"
lagomUnmanagedServices in ThisBuild += ("elasticsearch" -> "http://172.17.0.1:9200")
lagomCassandraEnabled in ThisBuild := false
