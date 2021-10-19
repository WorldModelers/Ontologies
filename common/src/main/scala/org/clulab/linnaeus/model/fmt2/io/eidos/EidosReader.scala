package org.clulab.linnaeus.model.fmt2.io.eidos

import org.clulab.linnaeus.model.fmt2.graph.eidos.EidosEdge
import org.clulab.linnaeus.model.fmt2.graph.eidos.EidosNetwork
import org.clulab.linnaeus.model.fmt2.graph.eidos.EidosNode
import org.clulab.linnaeus.model.fmt2.io.GraphReader
import org.yaml.snakeyaml.Yaml

import java.io.BufferedInputStream

class EidosReader(val network: EidosNetwork) extends GraphReader {

  protected def debugPrintln(text: String): Unit = println(text)

  def read(bufferedInputStream: BufferedInputStream): Unit = {
    val yaml = new Yaml()
    val loadedYaml = yaml.load(bufferedInputStream)
    val yamlNode = YamlNode.parse(loadedYaml)

    def walk(yamlNode: YamlNode, parentNodeOpt: Option[EidosNode]): EidosNode = {
      debugPrintln(yamlNode.name)

      val childNode = new EidosNode(network.nodeIndexer.next, yamlNode)

      network.addNode(childNode)
      parentNodeOpt.foreach { parentNode =>
        val edge = new EidosEdge(network.edgeIndexer.next)

        network.addEdge(parentNode.getId, edge, childNode.getId)
      }
      yamlNode.childrenOpt.foreach { children =>
        children.foreach { child =>
          walk(child, Some(childNode))
        }
      }
      childNode
    }

    walk(yamlNode, None)
  }
}
