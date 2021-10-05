package org.clulab.linnaeus.model.io.eidos

import scala.collection.JavaConverters._

import java.util.{ArrayList => JArrayList}
import java.util.{LinkedHashMap => JLinkedHashMap}


case class YamlNode(
  name: String,
  childrenOpt: Option[Array[YamlNode]],

  descriptionsOpt: Option[Array[String]],
  patternsOpt: Option[Array[String]],
  examplesOpt: Option[Array[String]],
  oppositeOpt: Option[String],
  polarityOpt: Option[Int],
  semanticTypeOpt: Option[String],
  others: Set[String]
) {

  def isLeaf: Boolean = childrenOpt.isEmpty || childrenOpt.get.isEmpty
}

object YamlNode {
  // alphabetical
  val CHILDREN = "children"
  val DESCRIPTIONS = "descriptions"
  val EXAMPLES = "examples"
  val NAME = "name"
  val NODE = "node"
  val OPPOSITE = "opposite"
  val PATTERNS = "patterns"
  val POLARITY = "polarity"
  val SEMANTIC_TYPE = "semantic type"

  // This is approximately the order they appear in the file.
  val KEYS = Set(NAME, DESCRIPTIONS, PATTERNS, EXAMPLES, OPPOSITE, POLARITY, SEMANTIC_TYPE, CHILDREN)

  def getNode(any: Any): JLinkedHashMap[String, Any] = any
      .asInstanceOf[JLinkedHashMap[String, Any]]
      .get(YamlNode.NODE)
      .asInstanceOf[JLinkedHashMap[String, Any]]

  def getString(node: JLinkedHashMap[String, Any], name: String): String =
      node.get(name).asInstanceOf[String]

  def getStringOpt(node: JLinkedHashMap[String, Any], name: String): Option[String] =
      Option(node.get(name).asInstanceOf[String])

  def getStringsOpt(node: JLinkedHashMap[String, Any], name: String): Option[Array[String]] =
      Option(node.get(name).asInstanceOf[JArrayList[String]]).map(_.asScala.toArray)

  def getIntOpt(node: JLinkedHashMap[String, Any], name: String): Option[Int] =
      // Without the map, null turns into 0.
      Option(node.get(name)).map(_.asInstanceOf[Integer])

  def parse(anyRef: Any): YamlNode = {
    val node = YamlNode.getNode(anyRef)
    val name = YamlNode.getString(node, NAME)
    val descriptionsOpt = YamlNode.getStringsOpt(node, DESCRIPTIONS)
    val patternsOpt = YamlNode.getStringsOpt(node, PATTERNS)
    val examplesOpt = YamlNode.getStringsOpt(node, EXAMPLES)
    val oppositeOpt = YamlNode.getStringOpt(node, OPPOSITE)
    val polarityOpt = YamlNode.getIntOpt(node, POLARITY)
    val semanticTypeOpt = YamlNode.getStringOpt(node, SEMANTIC_TYPE)
    val childrenOpt = Option(node.get(CHILDREN).asInstanceOf[JArrayList[Any]])
        .map(_.asScala)
        .map { children =>
          children.map { child =>
            parse(child)
          }.toArray
        }
    val others = {
      val keys = node.asScala.keys.toSet
      keys -- YamlNode.KEYS
    }

    new YamlNode(name, childrenOpt, descriptionsOpt, patternsOpt, examplesOpt, oppositeOpt, polarityOpt, semanticTypeOpt, others)
  }
}
