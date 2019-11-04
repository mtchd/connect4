name := "Connect4"

version := "0.3.0"

scalaVersion := "2.12.6"

// For Discord
libraryDependencies += "net.katsstuff" %% "ackcord" % "0.12.0"
// For Slack
libraryDependencies += "com.github.slack-scala-client" %% "slack-scala-client" % "0.2.5"
// For testing
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"

val circeVersion = "0.11.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

resolvers += Resolver.JCenterRepository

scalacOptions += "-Ypartial-unification" // 2.11.9+

libraryDependencies ++= {

  lazy val doobieVersion = "0.5.4"

  Seq(
    "org.tpolecat"          %% "doobie-core"            % doobieVersion,
    "org.tpolecat"          %% "doobie-h2"              % doobieVersion,
    "org.tpolecat"          %% "doobie-hikari"          % doobieVersion,
    "org.tpolecat"          %% "doobie-specs2"          % doobieVersion,
    "org.tpolecat"          %% "doobie-scalatest"       % doobieVersion       % "test",
    "mysql"                 % "mysql-connector-java"    % "5.1.34",
    "org.slf4j"             % "slf4j-api"               % "1.7.5",
    "ch.qos.logback"        % "logback-classic"         % "1.0.9"
  )

}

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

