package org.clulab.linnaeus.model.fmt2.graph.eidos

import org.clulab.linnaeus.model.fmt2.graph.GraphNode
import org.clulab.linnaeus.model.fmt2.io.eidos.YamlNode

class EidosNode(id: Long, yamlNode: YamlNode) extends GraphNode[Long](id) {

  def this(id: Long, name: String) = this(
    id,
    new YamlNode(name, None, None, None, None, None, None, None, Set.empty)
  )

  val name: String = yamlNode.name
  val descriptions: Array[String] = yamlNode.descriptionsOpt.getOrElse(Array.empty)
  val patterns: Array[String] = yamlNode.patternsOpt.getOrElse(Array.empty)
  val examples: Array[String] = yamlNode.examplesOpt.getOrElse(Array.empty)
  val oppositeOpt: Option[String] = yamlNode.oppositeOpt
  val polarityOpt: Option[Int] = yamlNode.polarityOpt
  val semanticTypeOpt: Option[String] = yamlNode.semanticTypeOpt
  val others: Set[String] = yamlNode.others
}
