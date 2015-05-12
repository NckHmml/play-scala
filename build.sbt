name := """play-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.scalikejdbc" %% "scalikejdbc"                       % "2.2.6",
  "org.scalikejdbc" %% "scalikejdbc-config"                % "2.2.6",
  "org.scalikejdbc" %% "scalikejdbc-play-plugin"           % "2.3.6"
)

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.18"

libraryDependencies += "io.really" %% "jwt-scala" % "1.2.2"