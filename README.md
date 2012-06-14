# Thorla

A Scala version of Thor:

https://github.com/wycats/thor

# Instructions

ThorlaMain uses Clapper to look for subclasses of Thorla. Unforunately, this does not play well with sbt at the moment. Therefore, as a fix, add this new task to your project's build.sbt (modified from Mark Harrah's answer on StackOverflow, http://stackoverflow.com/questions/7449312/create-script-with-classpath-from-sbt):

```scala
TaskKey[File]("mkrun") <<= (baseDirectory, javaOptions, fullClasspath in Runtime, mainClass in (Compile, run)) map { (base, opts, cp, main) =>
  val template = List("#!/bin/sh", """java %s -classpath "%s" %s "$@"""").mkString("\n")
  val mainStr = main getOrElse error("No main class specified")
  val contents = template.format(opts.mkString(" "), cp.files.absString, mainStr)
  val out = base / "/run.sh"
  IO.write(out, contents)
  out.setExecutable(true)
  out
}
```

This will allow you to create a shell script ("run.sh") which can run your project. "mkrun" should only need to be run when there is a change in the library dependencies.

# Roadmap

## 0.3

* Validates options and arguments

## Future Ideas

* Subcommand dependencies (allows chaining of subcommands)
* Parallelized subcommands
