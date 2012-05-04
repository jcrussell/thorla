name := "thorla"

version := "0.1"

scalaVersion := "2.9.1"

libraryDependencies += "com.github.scopt" %% "scopt" % "2.0.0"

libraryDependencies += "org.clapper" %% "classutil" % "0.4.6"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.7.2" % "test"

seq(ProguardPlugin.proguardSettings :_*)

proguardOptions ++= Seq(
  "-keep class thorla.** { *; }"
)
