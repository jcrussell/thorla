package thorla

object ThorlaMain {

  lazy private val thorlas = findThorlas()

  def main(args: Array[String]) {
    if(args.size == 0) {
      usage(thorlas)
    }
    else {
      thorla(args)
    }
  }

  private def thorla(args: Array[String]) = {
    args.head match {
      case "list" => { usage(thorlas) }
      case x => {
        thorlas.find(_.respondsTo(x)) match {
          case Some(thorla) => { thorla.invoke(x, args.tail) }
          case None => {
            println("Task '%s' not found".format(x))
            usage(thorlas)
          }
        }
      }
    }
  }

  private def usage(thorlas: List[Thorla]) = {
    val defaults = thorlas.filter(_.namespace == Thorla.defaultNamespace)
    val others = thorlas.filterNot(defaults.contains)

    if(defaults.size > 0) {
      printHeader("default")
      defaults.flatMap(_.usage().split("\n")).sortWith{_ < _}.foreach(println)
      println
    }

    others.sortWith(_.namespace < _.namespace).foreach(thorla => {
      printHeader(thorla.namespace)
      println(thorla.usage())
      println
    })

    0
  }

  private def printHeader(name: String) {
    println(name)
    println("-"*name.size)
  }

  private def findThorlas(): List[Thorla] = {
    import org.clapper.classutil.ClassFinder
    import org.clapper.classutil.ClassInfo
    import java.io.File

    val classpath = (findContainingJar(classOf[Thorla]) match {
      case Some(jar) => { "%s:".format(jar) }
      case _ => { "" }
    }) + System.getProperty("java.class.path")

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
          jar.replaceAll("!.*$", "") // remove special characters
        }
      }
    }}
  }
}
