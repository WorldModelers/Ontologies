package org.clulab.linnaeus.model.fmt1.io.eidos

object EidosIO {
  // This is approximately the order they appear in the file.
  val FIELD = "OntologyNode"
  val PATTERN = "pattern"
  val EXAMPLES = "examples"
  val DEFINITION = "definition"
  val DESCRIPTIONS = "descriptions"
  val NAME = "name"
  val OPPOSITE = "opposite"
  val POLARITY = "polarity"
  val SEMANTIC_TYPE = "semantic type"

  val keys = Set(FIELD, PATTERN, EXAMPLES, DEFINITION, DESCRIPTIONS, NAME, OPPOSITE, POLARITY, SEMANTIC_TYPE)
}

// definition