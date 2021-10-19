package org.clulab.linnaeus.model.fmt1.graph

class Indexer {
  protected var prevIndex: Int = -1

  def next: Int = {
    prevIndex += 1
    prevIndex
  }

  def reset: Unit = prevIndex = -1
}