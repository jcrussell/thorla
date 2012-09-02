package thorla

object ThorlaMain {

  lazy private val thorlas = findThorlas()

  def main(args: Array[String]) {
    if(args.size == 0) {
      usage(thorlas, "")
    }
    else {
      sys.exit(thorla(args))
    }
  }

  private def thorla(args: Array[String]) = {
    args.head match {
      case "list" => {
        val tail = args.tail
        usage(thorlas, if(tail.size > 0) { tail.head } else { "" })
        1
      }
      case x => {
        thorlas.find(_.respondsTo(x)) match {
          case Some(thorla) => { thorla.invoke(x, args.tail) }
          case None => {
            println("Task '%s' not found, looking for partial matches.".format(x))
            usage(thorlas, x)
            1
          }
        }
      }
    }
  }

  private def usage(thorlas: List[Thorla], partial: String) {
    val matched = thorlas.flatMap(_.usage).filter(_._1.startsWith(partial))

    if(matched.size > 0) {
      usage(matched)
    }
    else {
      println("No partial matches found. Usage:")
      usage(thorlas.flatMap(_.usage))
    }

    0
  }

  private def usage(usages: List[(String, String)]) {
    val namespaces = usages.map(_._1.split(":").head).toList.sorted.distinct

    namespaces.foreach(namespace => {
      val to_print = usages.filter(_._1.startsWith(namespace))
      printHeader(if(namespace == "") { "default" } else { namespace })

      // Calculate the longest usage
      val len = usages.map(_._1.size).max
      usages.foreach{case (usage, desc) => {
        println(("%-"+len+"s  # %s").format(usage, desc))
      }}

      println
    })
  }

  private def printHeader(name: String) {
    println(name)
    println("-"*name.size)
  }

  private def findThorlas(): List[Thorla] = {
    findContainingJar(classOf[Thorla]) match {
      case Some(jar) => { // Found a jar, could be thorla jar or application jar
        val thorlas = findThorlas("%s".format(jar))
        if(thorlas.size == 0) { // No tasks found, try looking through whole classpath
          findThorlas(System.getProperty("java.class.path"))
        } else { thorlas }
      }
      case _ => { // Didn't find any in containing jar, try looking through whole classpath
        findThorlas(System.getProperty("java.class.path"))
      }
    }
  }

  private def findThorlas(classpath: String): List[Thorla] = {
    import org.clapper.classutil.ClassFinder
    import org.clapper.classutil.ClassInfo
    import java.io.File

    val finder = ClassFinder(classpath.split(":").map(new File(_)))
    val subclasses = ClassFinder.concreteSubclasses("thorla.Thorla", finder.getClasses)
    subclasses.map(sub => {
      Class.forName(sub.name) match {
        case x:Class[Thorla] => {
          x.getField("MODULE$").get(null).asInstanceOf[Thorla]
        }
      }
    }).toList
  }

  private def findContainingJar(clazz: Class[_]): Option[String] = {
    import scala.collection.JavaConversions.enumerationAsScalaIterator
    import java.net.URLDecoder
    import java.net.URL

    val loader = clazz.getClassLoader
    val file = clazz.getName().replaceAll("\\.", "/") + ".class";

    loader.getResources(file).collectFirst{url: URL => {
      url.getProtocol match {
        case "jar" => {
          val jar = URLDecoder.decode(url.getPath.replace("file:", ""), "UTF-8")
          jar.replaceAll("!.*$", "") // remove classname from end of path
        }
      }
    }}
  }
}
