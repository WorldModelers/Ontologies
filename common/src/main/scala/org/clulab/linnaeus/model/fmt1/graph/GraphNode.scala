package org.clulab.linnaeus.model.fmt1.graph

class GraphNode[NodeIdentityType](
  protected val id: NodeIdentityType
) extends Identifyable[NodeIdentityType] {

  def getId: NodeIdentityType = id
}
