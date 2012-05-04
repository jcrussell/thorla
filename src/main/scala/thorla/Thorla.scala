package thorla

import scopt.mutable.OptionParser

trait Thorla {
  case class Option(sName: String, lName: String, description: String, default: Any)
  case class SubCommand(usage: String, description: String, numArgs: Int, var options: Array[Option])

  private var subCommands = Map[String, SubCommand]()

  private var currCommand = ""

  val defaultNamespace = ""

  /**
   * Where parsed option values go once they are parsed.
   */
  var parsedOptions = Map[String, Any]()

  /**
   * Subclasses must specify the namespace for subcommands, can use predefined defaultNamespace.
   */
  def namespace: String

  /**
   * Creates a new subcommand
   *
   * @param usage: subcommand usage string, first word is the invoke name and should be the same as the method name
   * @param description: description of the subcommand
   */
  final def desc(usage: String, description: String = "") {
    val parts = usage.split(" ")
    val invoke = parts.head
    val numArgs = parts.tail.size

    subCommands ++= Map(invoke -> new SubCommand(usage, description, numArgs, Array[Option]()))

    currCommand = invoke
  }

  /**
   * Adds an option to a subcommand previously named by calling desc
   *
   * @param sName: short-hand for option
   * @param lName: full name for option
   * @param description: description of option
   * @param default: default value, used to infer argument type. if not specified, assumes option is a flag.
   */
  final def options(sName: String, lName: String = "", description: String = "", default: Any = Unit) {
    if(currCommand == "") {
      Console.err.println("Thorla error: options appears before desc")
      return
    }

    subCommands(currCommand).options :+= new Option(sName, lName, description, default)
  }

  /**
   * Returns the usage for a specific subcommand or lists the available subcommands
   *
   * @param invoke: specify a subcommand to print usage for, by default prints all subcommands and descriptions
   */
  final def usage(invoke: String = ""): String = {
    if(invoke == "" || !subCommands.contains(invoke)) { // List the subcommands
      val lines = subCommands.values.map(sub => {
        ("%s:%s".format(namespace, sub.usage), sub.description)
      })
      // Calculate the longest usage
      val len = lines.map(_._1.size).max
      lines.map{case (usage, description) => {
        ("%-"+len+"s  # %s").format(usage, description)
      }}.mkString("\n")
    }
    else { // Usage for specific subcommand
      buildOptionParser(invoke).usage
    }
  }

  final def respondsTo(task: String): Boolean = {
    subCommands.keys.find(sub => {
      "%s:%s".format(namespace, sub) == task
    }).isDefined
  }

  /**
   * Builds the actual option parser for options
   *
   * @param invoke: name of the subcommand to build options for, must be in subCommands
   */
  private def buildOptionParser(invoke: String): OptionParser = {
    val parser = new OptionParser(invoke)

    subCommands(invoke).options.foreach(opt => {
      opt.default match {
        case x:Int => { parser.intOpt(opt.sName, opt.lName, opt.description, { updateValue(opt.sName, _) }) }
        case x:Double => { parser.doubleOpt(opt.sName, opt.lName, opt.description, { updateValue(opt.sName, _) }) }
        case x:Unit => {
          updateValue(opt.sName, false) // initially false (flag not seen)
          parser.opt(opt.sName, opt.lName, opt.description, { updateValue(opt.sName, true) })
        }
      }
    })
    parser.help("-h", "--help", "show this usage message and exit")

    parser
  }

  private def updateValue(name: String, value: Any) {
    parsedOptions += (name -> value)
  }

  final def invoke(task: String, args: Array[String]): Int = {
    0
  }
}
