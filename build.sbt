name := "thorla"

version := "0.1"

scalaVersion := "2.9.1"

seq(ProguardPlugin.proguardSettings :_*)

proguardOptions ++= Seq(
  "-keep class thorla.** { *; }",
)
