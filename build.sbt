// see https://github.com/sbt/sbt-assembly
import AssemblyKeys._ // put this at the top of the file

organization := "Micronautics Research"

name := "MeetupRoll"

version := "0.1"

scalacOptions ++= Seq("-deprecation")

logLevel := Level.Error

resolvers ++= Seq(
  "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots",
  "Typesafe Releases"  at "http://repo.typesafe.com/typesafe/releases",
  ScalaToolsSnapshots  // ScalaToolsReleases is included by default
)

libraryDependencies ++= Seq(
  "com.github.scala-incubator.io" %% "scala-io-core" % "latest.milestone"   withSources(),
  "com.github.scala-incubator.io" %% "scala-io-file" % "latest.milestone"   withSources(),
  "javax.mail"                    %  "mail"          % "latest.integration" withSources(),
  "org.scalatest"                 %% "scalatest"     % "latest.milestone"   % "test" withSources()
)

// add all the settings in assemblySettings into each SBT project
seq(assemblySettings: _*)
