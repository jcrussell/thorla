name := "thorla"

version := "0.4-SNAPSHOT"

organization := "com.github.thorla"

scalaVersion := "2.10.0"

resolvers ++= Seq(
  "sonatype-public" at "https://oss.sonatype.org/content"
)

libraryDependencies ++= Seq(
  "com.github.scopt" % "scopt_2.10" % "2.1.0",
  "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
  "org.slf4j" % "slf4j-nop" % "1.6.2"
)
