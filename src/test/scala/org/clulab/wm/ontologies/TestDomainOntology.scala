package org.clulab.wm.eidos.system

import org.clulab.linnaeus.model.graph.eidos.EidosNetwork
import org.clulab.linnaeus.model.graph.eidos.EidosNode
import org.clulab.linnaeus.model.io.eidos.EidosReader
import org.scalatest._

import scala.collection.mutable

class TestDomainOntology extends FlatSpec with Matchers {

  def hasDuplicates(network: EidosNetwork): Boolean = {
    val visitor = new network.HierarchicalGraphVisitor()
    var paths = List.empty[Seq[String]]
    var path = mutable.Seq.empty[String]
    var duplicate = false

    visitor.foreachNode { (node: EidosNode, depth: Int) =>
      if (network.isLeaf(node)) {
        val newPath = path.slice(0, depth) :+ node.name

        println(newPath)
        if (paths.contains(newPath)) {
          duplicate = true
          println(s"Duplicate: $newPath")
        }
        else
          paths = newPath :: paths
      }
      else
        if (depth < path.size)
          path(depth) = node.name
        else
          path = path :+ node.name
      true
    }
    duplicate
  }

  def hasSpaces(network: EidosNetwork): Boolean = {
    val visitor = new network.HierarchicalGraphVisitor()
    var path = mutable.Seq.empty[String]
    var spaces = false

    visitor.foreachNode { (node: EidosNode, depth: Int) =>
      if (node.name.contains(' ')) {
        val newPath = path.slice(0, depth) :+ node.name
        spaces = true
        println(s"Spaces: $newPath")
      }

      if (network.isLeaf(node)) {
        if (!node.name.contains(' ')) { // Otherwise already printed
          val newPath = path.slice(0, depth) :+ node.name
          println(newPath)
        }
      }
      else
        if (depth < path.size)
          path(depth) = node.name
        else
          path = path :+ node.name
        true
    }
    spaces
  }

  behavior of "ontologies"

  def test(path: String): Unit = {
    it should "load and not have duplicates or spaces in " + path in {
      val network = new EidosNetwork()
      val reader = new EidosReader(network)

      // This is the load part which will fail on exception.
      reader.readFromFile(path)
      println("Checking for duplicates")
      hasDuplicates(network) should be (false)
      println("Checking for spaces")
      hasSpaces(network) should be (false)
    }
  }

  Seq(
    "./wm_metadata.yml"
  ).foreach(path => test(path))
}
