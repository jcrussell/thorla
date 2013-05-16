package test.thorla

import thorla._

object ThorlaExample extends Thorla {

  override def namespace = "example"

  desc("foo", "test method foo")
  def foo() = {
    println("hello world")
    0
  }

  desc("with_arg STRING", "test method with_arg")
  def with_arg(arg: String) = {
    println("hello %s".format(arg))
    0
  }

  desc("with_int_arg NUMBER", "test method with_int_arg")
  def with_int_arg(arg: Int) = {
    println("hello %d".format(arg))
    0
  }

  desc("with_options", "test method with_options")
  options("a", desc = "first")
  options("b", "second", "second")
  options("c", "third", "third", 0)
  options("d", "fourth", "fourth", 1.0)
  def with_options() = {
    println("hello a: %b".format(options("a")))
    println("hello b: %b".format(options("b")))
    println("hello c: %d".format(options("c")))
    println("hello d: %f".format(options("d")))
    0
  }
}
