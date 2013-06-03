// see https://github.com/sbt/sbt-assembly
//import AssemblyKeys._ // put this at the top of the file

organization := "Micronautics Research"

name := "Meetup Roll"

version := "0.3"

scalaVersion := "2.10.1"

scalaVersion in update := "2.10"

javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked")

scalacOptions ++= Seq("-deprecation", "-unchecked", "-encoding", "utf8")

logLevel := Level.Error

resolvers ++= Seq(
  "Typesafe Releases"        at "http://repo.typesafe.com/typesafe/releases",
  "Scala-Tools Snapshots"    at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Scala Tools Releases"     at "https://oss.sonatype.org/content/groups/scala-tools/",
  "Sonatype Nexus Releases"  at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype Legacy Releases" at "https://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2" withSources(),
  "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2" withSources(),
  "javax.mail"                    %  "mail"          % "latest.integration" withSources(),
  "org.scala-tools.time"          %  "time_2.9.1"    % "0.5",
  "org.scala-lang"                %  "scala-swing"   % "2.10.1" withSources(),
  "org.scalatest"                 %% "scalatest"     % "latest.milestone"   % "test" withSources()
)

// add all the settings in assemblySettings into each SBT project
//seq(assemblySettings: _*)
