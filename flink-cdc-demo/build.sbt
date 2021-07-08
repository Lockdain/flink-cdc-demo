ThisBuild / resolvers ++= Seq(
  "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
  Resolver.mavenLocal
)

name := "Flink Project"

version := "0.1-SNAPSHOT"

organization := "org.example"

ThisBuild / scalaVersion := "2.12.14"

val flinkVersion = "1.13.1"

val flinkDependencies = Seq(
  "org.apache.flink"      %% "flink-scala"                    % flinkVersion % "provided",
  "org.apache.flink"      %% "flink-streaming-scala"          % flinkVersion % "provided",
  "com.alibaba.ververica" % "flink-connector-postgres-cdc"    % "1.4.0",
  "org.apache.flink"      %% "flink-connector-elasticsearch7" % flinkVersion,
  "org.apache.flink"      %% "flink-table-api-scala"          % flinkVersion,
  "org.apache.flink"      %% "flink-table-api-scala-bridge"   % flinkVersion
)

val circeVersion = "0.14.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

lazy val root = (project in file(".")).settings(
  libraryDependencies ++= flinkDependencies
)

assembly / mainClass := Some("ru.neoflex.flink.cdc.demo.PostgresCdcJob")

// make run command include the provided dependencies
Compile / run := Defaults.runTask(Compile / fullClasspath, Compile / run / mainClass, Compile / run / runner).evaluated

// stays inside the sbt console when we press "ctrl-c" while a Flink programme executes with "run" or "runMain"
Compile / run / fork := true
Global / cancelable := true

// exclude Scala library from assembly
assembly / assemblyOption := (assembly / assemblyOption).value.copy(includeScala = false)

// Jackson specific merge policies
assembly / assemblyMergeStrategy := {
  case PathList(ps @ _*) if ps contains "jackson"     => MergeStrategy.first
  case PathList("META-INF", "MANIFEST.MF")            => MergeStrategy.discard
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}
