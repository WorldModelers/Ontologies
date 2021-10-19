package org.clulab.linnaeus.model.fmt2.graph.eidos

import org.clulab.linnaeus.model.fmt2.graph.GraphEdge

class EidosEdge(id: Long, val relation: String = EidosEdge.RELATION)
    extends GraphEdge[Long](id)  {
}

object EidosEdge {
  val RELATION: String = "is_the_parent_of"
}
