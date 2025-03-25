name := "explicits-test"

scalaVersion := "3.2.0"
scalacOptions += "-Xmacro-settings:mx.m-k.explicits.debug"
crossScalaVersions := Seq(
  "3.2.0",
  "3.2.1",
  "3.2.2",
  "3.3.0",
  "3.3.1",
  "3.3.3",
  "3.4.0",
  "3.4.1",
  "3.4.2",
  "3.4.3",
  "3.5.0",
  "3.5.1",
  "3.5.2",
  "3.6.2",
  "3.6.3",
  "3.6.4"
)

ThisBuild / idePackagePrefix := Some("test")
Global / excludeLintKeys += idePackagePrefix

Test / scalaSource := file("src")
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.17" % Test
