name := "Connect4"

version := "0.1"

scalaVersion := "2.12.4"

// For Discord
libraryDependencies += "net.katsstuff" %% "ackcord" % "0.12.0"
// For Slack
libraryDependencies += "com.github.slack-scala-client" %% "slack-scala-client" % "0.2.5"
// For testing
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

resolvers += Resolver.JCenterRepository

