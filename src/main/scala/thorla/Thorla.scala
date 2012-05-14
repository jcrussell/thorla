package thorla

import scopt.mutable.OptionParser

import java.lang.reflect.Method

object Thorla {
  val defaultNamespace = ""
}

trait Thorla {
  case class SubCommandOption(sName: String, lName: String, desc: String, default: Any)
  case class SubCommand(usage: String, desc: String, numArgs: Int, method: Method, var options: Array[SubCommandOption])

  private var subCommands = Map[String, SubCommand]()

  private var currCommand = ""

  private var argValues = Array[String]()
  var options = Map[String, Any]()

  /**
   * Subclasses must specify the namespace for subcommands, can use predefined defaultNamespace.
   */
  def namespace: String

  /**
   * Creates a new subcommand
   *
   * @param usage: subcommand usage string, first word is the invoke name and should be the same as the method name
   * @param desc: description of the subcommand
   */
  final def desc(usage: String, desc: String = "") {
    val parts = usage.split(" ")
    val invoke = parts.head
    val numArgs = parts.tail.size

    if(subCommands.keys.find(_ == invoke).isDefined) {
      err("%s is already described and overloading is not supported".format(invoke))
    }
    else {
      getMethod(invoke) match {
        case Some(meth) => {
          if(numArgs != meth.getParameterTypes.size) {
            err("usage num args does not match num parameters for %s".format(usage))
          }
          else {
            subCommands ++= Map(invoke -> new SubCommand(usage, desc, numArgs, meth, Array[SubCommandOption]()))
            currCommand = invoke
          }
        }
        case None => {
          err("failed to find invoke target (%s)".format(invoke))
        }
      }
    }
  }

  /**
   * Adds an option to a subcommand previously named by calling desc
   *
   * @param sName: short-hand for option
   * @param lName: full name for option
   * @param desc: description of option
   * @param default: default value, used to infer argument type. if not specified, assumes option is a flag.
   */
  final def options(sName: String, lName: String = "", desc: String = "", default: Any = false) {
    if(currCommand == "") {
      err("options appears before desc")
      return
    }

    subCommands(currCommand).options :+= new SubCommandOption(sName, lName, desc, default)
  }

  /**
   * Returns the usage for a specific subcommand or lists the available subcommands
   *
   * @param invoke: specify a subcommand to print usage for, by default prints all subcommands and descriptions
   * @return usage string
   */
  final def usage(invoke: String = ""): String = {
    if(invoke == "" || !subCommands.contains(invoke)) { // List the subcommands
      val lines = subCommands.values.map(sub => {
        ("%s:%s".format(namespace, sub.usage), sub.desc)
      })
      // Calculate the longest usage
      val len = lines.map(_._1.size).max
      lines.map{case (usage, desc) => {
        ("%-"+len+"s  # %s").format(usage, desc)
      }}.mkString("\n")
    }
    else { // Usage for specific subcommand
      buildOptionParser(invoke).usage
    }
  }

  /**
   * Tests whether this Thorla responds to the given task
   *
   * @param task: a task of the form namespace:invoke to test for
   * @return true iff this thorla can run the given task
   */
  final def respondsTo(task: String): Boolean = {
    subCommands.keys.find(sub => {
      "%s:%s".format(namespace, sub) == task
    }).isDefined
  }

  /**
   * Builds the actual option parser for options
   *
   * @param invoke: name of the subcommand to build options for, must be in subCommands
   * @return OptionParser for the given invoke subcommand
   */
  private def buildOptionParser(invoke: String): OptionParser = {
    val parser = new OptionParser(invoke)
    val sub = subCommands(invoke)

    argValues = Array.fill(sub.numArgs){""}

    sub.options.foreach(opt => {
      setVal(opt.sName, opt.default) // initially default value
      opt.default match {
        case x:String => { parser.opt(opt.sName, opt.lName, opt.desc, { setVal(opt.sName, _) }) }
        case x:Int => { parser.intOpt(opt.sName, opt.lName, opt.desc, { setVal(opt.sName, _) }) }
        case x:Double => { parser.doubleOpt(opt.sName, opt.lName, opt.desc, { setVal(opt.sName, _) }) }
        case x:Boolean => { parser.booleanOpt(opt.sName, opt.lName, opt.desc, { setVal(opt.sName, _) }) }
        case _ => {
          err("unsupported option type: %s".format(opt.default.getClass.getName))
          sys.exit(1)
        }
      }
    })

    methodParams(invoke).foreach{case (arg,param,i) => {
      val desc = "%s is a %s".format(arg, param.getName)
      parser.arg(arg, desc, { setVal(i, _) })
    }}

    parser.help("h", "help", "show this usage message and exit")

    parser
  }

  /**
   * Invoke a task, should make sure this Thorla respondsTo the task first
   *
   * @param task: a task of the form namespace:invoke to start
   * @param args: additional arguments from the command line
   * @return return value of the subcommand or non-zero if error
   */
  final def invoke(task: String, args: Array[String]): Int = {
    val invoke = task.split(":")(1)
    val parser = buildOptionParser(invoke)
    if(parser.parse(args)) {
      val sub = subCommands(invoke)

      val methArgs = argValues.zip(sub.method.getParameterTypes).map{case (arg, param) => {
        if(classOf[Integer] isAssignableFrom param) { Int.box(arg.toInt) }
        else if(classOf[Double] isAssignableFrom param) { Double.box(arg.toDouble) }
        else if(classOf[String] isAssignableFrom param) { arg.toString }
        else {
          err("unsupported task method parameter type: %s".format(param.getName))
          sys.exit(1)
        }
      }}.toArray

      sub.method.invoke(this, methArgs : _* ).asInstanceOf[Int]
    }
    else {
      err("failed to parse args")
      1
    }
  }

  /*
   * Accessor methods for option values, helps with typing
   *
   * As long as the application programmer knows what they are doing, this won't cause the application to
   * crash...
   */

  def optionAsString(name: String): String = {
    options(name).asInstanceOf[String]
  }

  def optionAsInt(name: String): Int = {
    options(name).asInstanceOf[Int]
  }

  def optionAsDouble(name: String): Double = {
    options(name).asInstanceOf[Double]
  }

  def optionAsBoolean(name: String): Boolean = {
    options(name).asInstanceOf[Boolean]
  }

  private def err(msg: String) {
    Console.err.println("Thorla Error: '%s'".format(msg))
  }

  private def setVal(index: Int, value: String) {
    argValues(index) = value
  }

  private def setVal(name: String, value: Any) {
    options += (name -> value)
  }

  private def getMethod(name: String): Option[Method] = {
    this.getClass.getMethods.find(_.getName == name)
  }

  private def methodParams(invoke: String): Iterable[(String, Class[_], Int)] = {
    val args = subCommands(invoke).usage.split(" ").drop(1)
    val meth = subCommands(invoke).method
    args.zip(meth.getParameterTypes).zipWithIndex.map{case ((x,y),z) => (x,y,z)}
  }
}
