package org.clulab.linnaeus.model.io.eidos

import org.clulab.linnaeus.model.graph.eidos.EidosEdge
import org.clulab.linnaeus.model.graph.eidos.EidosNetwork
import org.clulab.linnaeus.model.graph.eidos.EidosNode
import org.clulab.linnaeus.model.io.GraphReader
import org.yaml.snakeyaml.Yaml

import scala.collection.JavaConverters._
import scala.collection.mutable

import java.io.BufferedInputStream
import java.util.{ArrayList => JArrayList}
import java.util.{Collection => JCollection}
import java.util.{LinkedHashMap => JLinkedHashMap}
import java.util.{Map => JMap}

class EidosReader(val network: EidosNetwork) extends GraphReader {

  protected def debugPrintln(text: String): Unit = println(text)

  def getNode(any: Any): JLinkedHashMap[String, Any] = any
      .asInstanceOf[JLinkedHashMap[String, Any]]
      .get(EidosIO.NODE)
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

  def read(bufferedInputStream: BufferedInputStream): EidosNode = {
    val yaml = new Yaml()
    val loadedYaml = yaml.load(bufferedInputStream)
    val yamlNode = YamlNode.parse(loadedYaml)

    def walk(yamlNode: YamlNode, parentNodeOpt: Option[EidosNode]): Unit = {
      val childNode = new EidosNode(network.nodeIndexer.next, yamlNode, others)
      network.addNode(childNode)
      parentNodeOpt.foreach { parentNode =>
        val edge = new EidosEdge(network.edgeIndexer.next)

        network.addEdge(parentNode.getId, edge, childNode.getId)
      }
      yamlNode.childrenOpt.foreach { children =>
        children.foreach { child =>
          val eidosNode = walk(child, Some(childNode))
          chidlNode.
        }
      }
    }

    walk(yamlNode, None)
  }
}

case class YamlNode(
  name: String,
  childrenOpt: Option[Array[YamlNode]],

  descriptionsOpt: Option[Array[String]],
  patternsOpt: Option[Array[String]],
  examplesOpt: Option[Array[String]],
  oppositeOpt: Option[String],
  polarityOpt: Option[Int],
  semanticTypeOpt: Option[String]
) {

  def isLeaf: Boolean = childrenOpt.isEmpty || childrenOpt.get.isEmpty
}

object YamlNode {
  val NODE = "node"
  val NAME = "name"
  val CHILDREN = "children"
  val DESCRIPTIONS = "descriptions"
  val PATTERNS = "patterns"
  val EXAMPLES = "examples"
  val OPPOSITE = "opposite"
  val POLARITY = "polarity"
  val SEMANTIC_TYPE = "semantic type"

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

    new YamlNode(name, childrenOpt, descriptionsOpt, patternsOpt, examplesOpt, oppositeOpt, polarityOpt, semanticTypeOpt)
  }
}
