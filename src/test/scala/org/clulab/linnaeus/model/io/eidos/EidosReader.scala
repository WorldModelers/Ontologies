package org.clulab.linnaeus.model.io.eidos

import java.io.BufferedInputStream
import java.util.{Collection => JCollection}
import java.util.{Map => JMap}

import org.clulab.linnaeus.model.graph.eidos.EidosEdge
import org.clulab.linnaeus.model.graph.eidos.EidosNetwork
import org.clulab.linnaeus.model.graph.eidos.EidosNode
import org.clulab.linnaeus.model.io.GraphReader
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

import scala.collection.JavaConverters._
import scala.collection.mutable

class EidosReader(val network: EidosNetwork) extends GraphReader {

  def read(bufferedInputStream: BufferedInputStream): Unit = {
    val yaml = new Yaml(new Constructor(classOf[JCollection[Any]]))
    val yamlNodes = yaml.load(bufferedInputStream).asInstanceOf[JCollection[Any]].asScala

    def yamlNodesToStrings(yamlNodes: mutable.Map[String, JCollection[Any]], name: String): Seq[String] =
      yamlNodes.get(name).map(_.asInstanceOf[JCollection[String]].asScala.toSeq).getOrElse(Seq.empty)

    // This code is largely stolen from Eidos
    def parseYamlLeaf(parentNodeOpt: Option[EidosNode], yamlNodes: mutable.Map[String, JCollection[Any]]): Unit = {
      val name = yamlNodes(EidosIO.NAME).asInstanceOf[String]
      val oppositeOpt = yamlNodes.get(EidosIO.OPPOSITE).asInstanceOf[Option[String]]
      val polarityOpt = yamlNodes.get(EidosIO.POLARITY).asInstanceOf[Option[Int]]
      val examples = yamlNodesToStrings(yamlNodes, EidosIO.EXAMPLES)
      val descriptions = yamlNodesToStrings(yamlNodes, EidosIO.DESCRIPTION)
      // These need to be valid regexes, but don't check that just yet.
      val patterns = yamlNodesToStrings(yamlNodes, EidosIO.PATTERN)
      val childNode = new EidosNode(network.nodeIndexer.next, name, oppositeOpt, polarityOpt, examples, descriptions, patterns)

      network.addNode(childNode)
      parentNodeOpt.foreach { parentNode =>
        val edge = new EidosEdge(network.edgeIndexer.next)

        network.addEdge(parentNode.getId, edge, childNode.getId)
      }
    }

    def parseYamlBranchOrLeaf(parentNodeOpt: Option[EidosNode], yamlNodes: Iterable[Any]): Unit = {
      yamlNodes.foreach { yamlNode =>
        if (yamlNode.isInstanceOf[String])
          throw new Exception(s"Ontology has string (${yamlNode.asInstanceOf[String]}) where it should have a map.")
        val map: mutable.Map[String, JCollection[Any]] = yamlNode.asInstanceOf[JMap[String, JCollection[Any]]].asScala
        val key: String = map.keys.head

        if (key == EidosIO.FIELD)
          parseYamlLeaf(parentNodeOpt, map)
        else {
          val childNode = new EidosNode(network.nodeIndexer.next, key)

          network.addNode(childNode)
          parentNodeOpt.foreach { parentNode =>
            val edge = new EidosEdge(network.edgeIndexer.next)

            network.addEdge(parentNode.getId, edge, childNode.getId)
          }

          // This is to account for leafless branches.
          val yamlNodesOpt = Option(map(key).asScala)
          if (yamlNodesOpt.nonEmpty) // foreach does not work well here.
            parseYamlBranchOrLeaf(Some(childNode), yamlNodesOpt.get.toSeq)
        }
      }
    }

    parseYamlBranchOrLeaf(None, yamlNodes)
  }
}
