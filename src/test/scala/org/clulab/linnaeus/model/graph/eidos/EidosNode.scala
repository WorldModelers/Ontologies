package org.clulab.linnaeus.model.graph.eidos

import org.clulab.linnaeus.model.graph.GraphNode

class EidosNode(id: Long, val name: String, val polarityOpt: Option[Double] = None, val examples: Seq[String] = Seq.empty,
    val descriptions: Seq[String] = Seq.empty, val patterns: Seq[String] = Seq.empty) extends GraphNode[Long](id)
