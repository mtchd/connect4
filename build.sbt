import sbt.util

name := "Connect4"

version := "0.3.1"

scalaVersion := "2.12.6"

// For Discord
libraryDependencies += "net.katsstuff" %% "ackcord" % "0.12.0"
// For Slack
libraryDependencies += "com.github.slack-scala-client" %% "slack-scala-client" % "0.2.5"
// For testing
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
// For parsing
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"

val circeVersion = "0.11.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

resolvers += Resolver.JCenterRepository

// Doobie stuff
scalacOptions += "-Ypartial-unification" // 2.11.9+

lazy val doobieVersion = "0.8.4"

libraryDependencies ++= Seq(
  "org.tpolecat" %% "doobie-core"     % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-specs2"   % doobieVersion
)
// End Doobie stuff

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

logLevel := Level.Debug

