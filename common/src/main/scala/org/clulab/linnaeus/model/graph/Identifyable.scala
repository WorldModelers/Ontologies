package org.clulab.linnaeus.model.graph

trait Identifyable[IdentityType] {
  def getId: IdentityType
}
