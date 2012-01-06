organization := "Micronautics Research"

name := "MeetupRoll"

version := "0.1-SNAPSHOT"

scalacOptions ++= Seq("-deprecation")

resolvers ++= Seq(
  "Typesafe Snapshots"    at "http://repo.typesafe.com/typesafe/snapshots",
  "Typesafe Releases"     at "http://repo.typesafe.com/typesafe/releases",
  "Scala-Tools Snapshots" at "http://scala-tools.org/repo-snapshots",
  "Scala Tools Releases"  at "http://scala-tools.org/repo-releases"
)

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" %  "httpclient" % "latest.integration" % "compile" withSources(),
  "org.scalatest"             %% "scalatest"  % "latest.integration"  % "test"    withSources()
)
