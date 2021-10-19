package org.clulab.linnaeus.model.fmt1.io.eidos

import org.clulab.linnaeus.model.fmt1.graph.eidos.EidosEdge
import org.clulab.linnaeus.model.fmt1.graph.eidos.EidosNoMetaNetwork
import org.clulab.linnaeus.model.fmt1.graph.eidos.EidosNoMetaNode
import org.clulab.linnaeus.model.fmt1.io.GraphReader
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

import java.io.BufferedInputStream
import java.util.{Collection => JCollection}
import java.util.{Map => JMap}
import scala.collection.JavaConverters._
import scala.collection.mutable

class EidosNoMetaReader(val network: EidosNoMetaNetwork) extends GraphReader {

  protected def debugPrintln(text: String): Unit = println(text)
  
  def read(bufferedInputStream: BufferedInputStream): Unit = {
    val yaml = new Yaml(new Constructor(classOf[JCollection[Any]]))
    val yamlNodes = yaml.load(bufferedInputStream).asInstanceOf[JCollection[Any]].asScala

    def parseYamlBranchOrLeaf(parentNodeOpt: Option[EidosNoMetaNode], yamlNodes: Iterable[Any]): Unit = {
      yamlNodes.foreach {
        case name: String =>
          if (parentNodeOpt.isEmpty)
            throw new Exception(s"Ontology has string ($name) where it should have a map.")

          val childNode = new EidosNoMetaNode(network.nodeIndexer.next, name)

          debugPrintln(s"Adding leaf node for $name")
          network.addNode(childNode)
          parentNodeOpt.foreach { parentNode =>
            val edge = new EidosEdge(network.edgeIndexer.next)

            network.addEdge(parentNode.getId, edge, childNode.getId)
          }

        case jMap: JMap[_, _] =>
          val map: mutable.Map[String, JCollection[Any]] = jMap.asInstanceOf[JMap[String, JCollection[Any]]].asScala
          val key: String = map.keys.head
          val childNode = new EidosNoMetaNode(network.nodeIndexer.next, key)

          debugPrintln(s"Adding non-leaf node for $key")
          network.addNode(childNode)
          parentNodeOpt.foreach { parentNode =>
            val edge = new EidosEdge(network.edgeIndexer.next)

            network.addEdge(parentNode.getId, edge, childNode.getId)
          }

          // This is to account for leafless branches.
          val yamlNodesOpt = Option(map(key).asScala)
          if (yamlNodesOpt.nonEmpty) // foreach does not work well here.
            parseYamlBranchOrLeaf(Some(childNode), yamlNodesOpt.get.toSeq)

        case yamlNode =>
          throw new Exception(s"Unexpected yaml node encountered: $yamlNode")
      }
    }

    parseYamlBranchOrLeaf(None, yamlNodes)
  }
}
