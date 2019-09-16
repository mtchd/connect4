name := "Connect4"

version := "0.3.0"

scalaVersion := "2.12.4"

// For Discord
libraryDependencies += "net.katsstuff" %% "ackcord" % "0.12.0"
// For Slack
libraryDependencies += "com.github.slack-scala-client" %% "slack-scala-client" % "0.2.5"
// For testing
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

resolvers += Resolver.JCenterRepository

scalacOptions += "-Ypartial-unification" // 2.11.9+

lazy val doobieVersion = "0.7.0"

libraryDependencies ++= Seq(
  "org.tpolecat" %% "doobie-core"     % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-specs2"   % doobieVersion
)

