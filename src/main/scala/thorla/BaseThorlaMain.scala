package thorla

/**
 * BaseThorlaMain contains methods to run Thorlas and print out usage statements.
 * It doesn't know how to find the Thorlas - that is left to the "real" main classes.
 * This allows for both dynamic, class loader inspecting objects like ThorlaClapperMain
 * and for static, fixed list of Thorlas defined in an application's code.
 */
object BaseThorlaMain {

  def main(thorlas: List[Thorla], args: Array[String]) {
    args.headOption match {
      case Some("list") => {
        usage(thorlas, args.tail.headOption)
      }
      case Some(x) => {
        thorlas.find(_.respondsTo(x)) match {
          case Some(thorla) => {
            sys.exit(thorla.invoke(x, args.tail))
          }
          case None => {
            println("Task '%s' not found, looking for partial matches.".format(x))
            usage(thorlas, Some(x))
          }
        }
      }
      case None => {
        usage(thorlas)
      }
    }
  }

  private def usage(thorlas: List[Thorla], partial: Option[String] = None) {
    partial match {
      case Some(partial) => {
        val matched = thorlas.flatMap(_.usage).filter(_._1.startsWith(partial))

        if(matched.size > 0) {
          usage(matched)
        }
        else {
          println("No partial matches found. Full usage:")
          usage(thorlas.flatMap(_.usage))
        }
      }
      case None => {
        usage(thorlas.flatMap(_.usage))
      }
    }
  }

  private def usage(usages: List[(String, String)]) {
    val namespaces = usages.map(_._1.split(":").head).toList.sorted.distinct

    namespaces.foreach(namespace => {
      val invoke_prefix = "%s:".format(namespace)
      val to_print = usages.filter(_._1.startsWith(invoke_prefix)).sortWith{_._1 < _._1}
      printHeader(if(namespace == "") { "default" } else { namespace })

      // Calculate the longest usage
      val len = to_print.map(_._1.size).max
      to_print.foreach{case (usage, desc) => {
        println(("%-"+len+"s  # %s").format(usage, desc))
      }}

      println
    })
  }

  private def printHeader(name: String) {
    println(name)
    println("-"*name.size)
  }
}
