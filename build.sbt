organization := "lt.tabo"

name := "json2s-web"

version := "0.1.0-SNAPSHOT"

lazy val core = project in file("core")

lazy val root = (project in file(".")).enablePlugins(PlayScala).dependsOn(core)

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws
)


fork in run := true
