name := "fs2scala"

version := "0.1"

scalaVersion := "2.13.1"

// available for Scala 2.11, 2.12
libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % "2.1.0",
  "co.fs2" %% "fs2-io" % "2.1.0",
)
