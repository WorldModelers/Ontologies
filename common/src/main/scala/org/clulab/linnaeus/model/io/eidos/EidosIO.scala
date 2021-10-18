package org.clulab.linnaeus.model.io.eidos

object EidosIO {
  // alphabetical
  val DESCRIPTIONS = "descriptions"
  val CHILDREN = "children"
  val EXAMPLES = "examples"
  val NAME = "name"
  val NODE = "node"
  val OPPOSITE = "opposite"
  val PATTERNS = "patterns"
  val POLARITY = "polarity"
  val SEMANTIC_TYPE = "semantic type"

  // This is approximately the order they appear in the file.
  val keys = Set(NODE, NAME, DESCRIPTIONS, PATTERNS, EXAMPLES, OPPOSITE, POLARITY, SEMANTIC_TYPE)
}
