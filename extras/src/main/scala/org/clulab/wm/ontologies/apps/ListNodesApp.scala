package org.clulab.wm.ontologies.apps

import org.clulab.linnaeus.model.fmt2.graph.eidos.{EidosNetwork, EidosNode}
import org.clulab.linnaeus.model.fmt2.io.eidos.EidosReader
import org.clulab.wm.eidos.utils.FileUtils

import scala.collection.mutable

object ListNodesApp extends App {
  type Path = mutable.Seq[String]

  val inputFile = args.lift(0).getOrElse("./CompositionalOntology_metadata.yml")

  // If f returns true, it is tracked in overallResult and returned.
  protected def visit(network: EidosNetwork)(f: (EidosNode, Path) => Boolean): Boolean = {
    val visitor = new network.HierarchicalGraphVisitor()
    var path = mutable.Seq.empty[String]
    var overallResult = false

    visitor.foreachNode { (node: EidosNode, depth: Int) =>
      if (network.isLeaf(node)) {
        val newPath = path.slice(0, depth) :+ node.name

//        trace(path)
        if (f(node, newPath))
          overallResult = true
      }
      else
        if (depth < path.size)
          path(depth) = node.name
        else
          path = path :+ node.name
      true
    }
    overallResult
  }

  val network = {
    val network = new EidosNetwork()
    val reader = new EidosReader(network)
    val text = FileUtils.getTextFromFile(inputFile)

    reader.readFromString(text)
    network
  }

  val unknownKeys = visit(network) { (node: EidosNode, path: Path) =>
    val name = path.mkString("/")

    println(name)
    false
  }
}
