name := "thorla"

version := "0.1"

scalaVersion := "2.9.1"

libraryDependencies += "com.github.scopt" %% "scopt" % "2.0.0"

seq(ProguardPlugin.proguardSettings :_*)

proguardOptions ++= Seq(
  "-keep class thorla.** { *; }"
)
