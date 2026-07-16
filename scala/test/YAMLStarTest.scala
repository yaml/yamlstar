//> using scala 3.7.3

package org.yamlstar

object YAMLStarTest:
  def main(args: Array[String]): Unit =
    val data = YAMLStar.load("test: 42")
    assert(data("test").num.toInt == 42)
    println("ok - load yaml")
