package thorla

object ThorlaMain {

  final private val DEBUG = true

  class ThorlaWrapper(subClass: Class[Thorla]) {
    def namespace = {
      subClass.getMethod("namespace").invoke(null).asInstanceOf[String]
    }
    def usage() = {
      subClass.getMethod("usage").invoke(null, "").asInstanceOf[String]
    }
    def respondsTo(a1: String) = {
      subClass.getMethod("respondsTo").invoke(null, a1).asInstanceOf[Boolean]
    }
    def invoke(a1: String, a2: Array[String]) = {
      subClass.getMethod("namespace").invoke(null, a1, a2).asInstanceOf[Int]
    }
  }

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
          case task: Thorla => { task.invoke(x, args.tail) }
          case _ => { usage(thorlas) }
        }
      }
    }
  }

  private def usage(thorlas: List[ThorlaWrapper]) = {
    thorlas.foreach(thorla => {
      println(thorla.namespace)
      println("-"*thorla.namespace.size)
      println(thorla.usage())
    })

    1
  }

  private def findThorlas(): List[ThorlaWrapper] = {
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
        println("Found class: %s".format(sub))
      }
      new ThorlaWrapper(Class.forName(sub.name).asInstanceOf[Class[Thorla]])
    }).toList
  }
}
