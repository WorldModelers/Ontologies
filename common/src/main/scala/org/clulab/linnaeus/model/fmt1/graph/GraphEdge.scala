package org.clulab.linnaeus.model.fmt1.graph

class GraphEdge[EdgeIdentityType](
  val id: EdgeIdentityType
) extends Identifyable[EdgeIdentityType] {

  def getId: EdgeIdentityType = id
}
