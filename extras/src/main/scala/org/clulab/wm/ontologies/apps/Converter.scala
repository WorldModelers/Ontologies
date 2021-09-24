package org.clulab.wm.ontologies.apps

import org.clulab.linnaeus.model.graph.eidos.EidosNetwork
import org.clulab.linnaeus.model.graph.eidos.EidosNode
import org.clulab.linnaeus.model.io.eidos.EidosReader

import scala.collection.mutable

object Converter extends App {

  def toYValue(node: EidosNode): String = {
    ""
  }

  val inputFile = "CompositionalOntology_metadata.yml"
  val outputFile = "Revised-CompositionalOntology_metadata.yml"
  val network = {
    val network = new EidosNetwork()
    val reader = new EidosReader(network)

    reader.readFromFile(inputFile)
    network
  }
  val rootNode = network.getRootNode.get
  val jRootNode = toYValue(rootNode)
}

class Node(eidosNode: EidosNode) {
  val data = new mutable.LinkedHashMap[String, Any]()

  data("name") = eidosNode.name // required
  data("descriptions") = eidosNode.descriptions
  data("examples") = eidosNode.examples
  data("patterns") = eidosNode.patterns
  // opposite: [String]
  eidosNode.oppositeOpt.foreach { opposite =>
    data("opposite") = opposite
  }
  // polarity: [1|-1]
  eidosNode.polarityOpt.foreach { polarity =>
    data("polarity") = polarity
  }
  // semantic type: [entity | event | property]
  eidosNode.semanticTypeOpt.foreach { semanticType =>
    data("semantic type") = semanticType
  }
  data("children") = "" // multiple, last, recursion // optional or empty
  // Something from OntologyNode
}
