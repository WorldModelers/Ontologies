package org.clulab.linnaeus.model.graph

class GraphEdge[EdgeIdentityType](
  val id: EdgeIdentityType
) extends Identifyable[EdgeIdentityType] {

  def getId: EdgeIdentityType = id
}
