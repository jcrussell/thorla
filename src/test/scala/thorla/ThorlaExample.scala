package test.thorla

import thorla._

object ThorlaExample extends Thorla {

  override def namespace = "example"

  desc("foo", "test method foo")
  def foo() = {
    println("hello world")
  }
}
