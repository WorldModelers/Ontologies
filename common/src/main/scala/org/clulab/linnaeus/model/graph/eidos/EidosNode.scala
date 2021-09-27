package org.clulab.linnaeus.model.graph.eidos

import org.clulab.linnaeus.model.graph.GraphNode

class EidosNode(id: Long, val name: String, val oppositeOpt: Option[String] = None, val polarityOpt: Option[Int] = None, val examples: Seq[String] = Seq.empty,
    val descriptions: Seq[String] = Seq.empty, val patterns: Seq[String] = Seq.empty, val semanticTypeOpt: Option[String] = None,
    val definitionOpt: Option[String] = None, val others: Set[String] = Set.empty) extends GraphNode[Long](id)
