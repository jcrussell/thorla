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
      case "list" => {

      }
      case x => {
        thorlas.find(_.respondsTo(x)) match {
          case task: Thorla => { task.invoke(x, args.tail) }
          case _ => { usage(thorlas) }
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
  }

  private def findThorlas(): List[Thorla] = {
    // TODO: Get things that mix in Thorla trait
    List[Thorla]()
  }
}
