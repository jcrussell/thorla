# Thorla

A Scala version of Thor:

https://github.com/wycats/thor

# Instructions

Thorla builds a list of tasks with the help of a simple Main object maintainted by the application developer. Here's an example:

```scala
package example

object Main {
  lazy val thorlas = List(
    example.SolveWorldHunger,
    example.RepelAlienInvasion
  )

  def main(args: Array[String]) {
    thorla.BaseThorlaMain.main(thorlas, args)
  }
}
```

# Helpful sbt task for creating a run script:

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

## Future Ideas

* Validates options and arguments
* Subcommand dependencies (allows chaining of subcommands)
* Parallelized subcommands
