name := "thorla"

version := "0.1"

scalaVersion := "2.9.1"

seq(ProguardPlugin.proguardSettings :_*)

proguardOptions ++= Seq(
  "-keep class com.ibm.wala.** { *; }",
  "-keep class org.eclipse.** { *; }",
  "-keep class org.osgi.** { *; }",
  "-keep class com.mysql.jdbc.** { *; }",
  "-keep class com.sherlockdroid.deduction.Main { static void main(java.lang.String[]); static int run(java.lang.String[]); }"
)
