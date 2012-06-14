name := "thorla"

version := "0.2"

scalaVersion := "2.9.1"

libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % "2.0.0",
  "org.clapper" %% "classutil" % "0.4.6",
  "org.scalatest" %% "scalatest" % "1.7.2" % "test",
  "org.slf4j" % "slf4j-nop" % "1.6.2"
)

seq(ProguardPlugin.proguardSettings :_*)

proguardOptions ++= Seq(
  "-keep class thorla.** { *; }",
  "-keep class org.slf4j.** { *; }"
)
