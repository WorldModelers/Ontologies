package org.clulab.linnaeus.model.graph.eidos

import org.clulab.linnaeus.model.graph.GraphNode
import org.clulab.linnaeus.model.io.eidos.YamlNode

class EidosNode(id: Long, yamlNode: YamlNode) extends GraphNode[Long](id) {
  val name: String = yamlNode.name
  val descriptions: Seq[String] = yamlNode.descriptionsOpt.getOrElse(Array.empty)
  val patterns: Seq[String] = yamlNode.patternsOpt.getOrElse(Array.empty)
  val examples: Seq[String] = yamlNode.examplesOpt.getOrElse(Array.empty)
  val oppositeOpt: Option[String] = yamlNode.oppositeOpt
  val polarityOpt: Option[Int] = yamlNode.polarityOpt
  val semanticTypeOpt: Option[String] = yamlNode.semanticTypeOpt
  val others: Set[String] = yamlNode.others
}
