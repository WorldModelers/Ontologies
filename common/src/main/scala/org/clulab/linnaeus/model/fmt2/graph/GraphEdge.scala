package org.clulab.linnaeus.model.fmt2.graph

class GraphEdge[EdgeIdentityType](
  val id: EdgeIdentityType
) extends Identifyable[EdgeIdentityType] {

  def getId: EdgeIdentityType = id
}
