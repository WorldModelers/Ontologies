package org.clulab.wm.ontologies.apps

import org.clulab.linnaeus.model.fmt1.graph.eidos.EidosNetwork
import org.clulab.linnaeus.model.fmt1.io.eidos.EidosReader
import org.clulab.wm.eidos.utils.FileUtils

object OntologyStatsApp extends App {
  val inputFile = args.lift(0).getOrElse("./CompositionalOntology_metadata.yml")

  val network = {
    val network = new EidosNetwork()
    val reader = new EidosReader(network)
    val text = FileUtils.getTextFromFile(inputFile)

    reader.readFromString(text)
    network
  }

  var nodeCount = 0
  var exemplarCount = 0

  new network.HierarchicalGraphVisitor().foreachNode { eidosNode =>
    println(eidosNode.name)
    nodeCount += 1
    exemplarCount += eidosNode.examples.length
    true
  }
  println(s"nodeCount = $nodeCount")
  println(s"exemplarCount = $exemplarCount")
}
