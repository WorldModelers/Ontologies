package org.clulab.linnaeus.model.graph.eidos

import org.clulab.linnaeus.model.graph.GraphNode

class EidosNode(id: Long, val name: String, val oppositeOpt: Option[String] = None, val polarityOpt: Option[Int] = None, val examples: Seq[String] = Seq.empty,
    val descriptions: Seq[String] = Seq.empty, val patterns: Seq[String] = Seq.empty) extends GraphNode[Long](id)
