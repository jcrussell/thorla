package thorla

trait Thorla {
  // TODO: Figure out what we should use for command line options
  // http://stackoverflow.com/questions/2315912/scala-best-way-to-parse-command-line-parameters-cli
  case class Option(sName: String, lName: String, description: String, default: Any)
  case class SubCommand(usage: String, description: String, numArgs: Int, var options: Array[Option])

  private var subCommands = Map[String, SubCommand]()

  private var currCommand = ""

  val defaultNamespace = ""

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
  final def options(sName: String, lName: String = "", description: String = "", default: Any = Nil) {
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
  final def usage(invoke: String = "") = {
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
      buildOptions(invoke)//.usage
    }
  }

  /**
   * Builds the actual option parser for options
   *
   * @param invoke: name of the subcommand to build options for, must be in subCommands
   */
  private def buildOptions(invoke: String) = {
    // TODO
    ""
  }
}
