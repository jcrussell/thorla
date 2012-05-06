package thorla

object ThorlaMain {

  final private val DEBUG = true

  lazy private val thorlas = findThorlas()

  def main(args: Array[String]) {
    println("Args: [%s]".format(args.mkString(", ")))
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
          case Some(task) => { task.invoke(x, args.tail) }
          case None => {
            println("Task '%s' not found".format(x))
            usage(thorlas)
          }
        }
      }
    }
  }

  private def usage(thorlas: List[Thorla]) = {
    thorlas.foreach(thorla => {
      println(thorla.namespace)
      println("-"*thorla.namespace.size)
      println(thorla.usage())
    })

    1
  }

  private def findThorlas(): List[Thorla] = {
    import org.clapper.classutil.ClassFinder
    import org.clapper.classutil.ClassInfo
    import java.io.File

    val classpath = System.getProperty("java.class.path")
    if(DEBUG) {
      println("Classpath: %s".format(classpath))
    }
    val finder = ClassFinder(classpath.split(":").map(new File(_)))
    val subclasses = ClassFinder.concreteSubclasses("thorla.Thorla", finder.getClasses)
    subclasses.map(sub => {
      if(DEBUG) {
        println("Found sbclass: %s".format(sub))
      }
      val thorlaClass = Class.forName(sub.name).asInstanceOf[Class[Thorla]]
      thorlaClass.getField("MODULE$").get(null).asInstanceOf[Thorla]
    }).toList
  }
}
