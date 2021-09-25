package org.clulab.wm.ontologies.apps

import org.clulab.linnaeus.model.graph.eidos.EidosNetwork
import org.clulab.linnaeus.model.graph.eidos.EidosNode
import org.clulab.linnaeus.model.io.eidos.EidosReader
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.nodes.Tag

import scala.collection.JavaConverters._
import java.io.StringWriter
import java.util.{ArrayList => JArrayList}
import java.util.{IdentityHashMap => JIdentityHashMap}
import java.util.{List => JList}
import java.util.{LinkedHashMap => JLinkedHashMap}

object Converter extends App {
  val inputFile = "./CompositionalOntology_metadata.yml"
  val outputFile = "./Revised-CompositionalOntology_metadata.yml"
  val network = {
    val network = new EidosNetwork()
    val reader = new EidosReader(network)

    reader.readFromFile(inputFile)
    network
  }
  val eidosToLocalMap = new JIdentityHashMap[EidosNode, LocalNode]()

  def getOrAddNode(eidosNode: EidosNode): LocalNode = {
    Option(eidosToLocalMap.get(eidosNode)).getOrElse {
      val localNode = new LocalNode(eidosNode)

      eidosToLocalMap.put(eidosNode, localNode)
      localNode
    }
  }

  new (network.HierarchicalGraphVisitor).foreachEdge { case (eidosParentNode, _, eidosChildNode) =>
    val localParentNode = getOrAddNode(eidosParentNode)
    val localChildNode = getOrAddNode(eidosChildNode)

    localParentNode.addChild(localChildNode)
    true
  }

  val localRoot = eidosToLocalMap.get(network.getRootNode.get)
  val yaml = new Yaml().dumpAs(localRoot, Tag.MAP, null)

  println(yaml)
}

class LocalNode(eidosNode: EidosNode) extends JLinkedHashMap[String, Any] {
  val data = this
  val children = new JArrayList[LocalNode]()

  data.put("name", eidosNode.name) // required
  if (eidosNode.descriptions.nonEmpty)
    data.put("descriptions", eidosNode.descriptions.asJava)
  if (eidosNode.examples.nonEmpty)
    data.put("examples", eidosNode.examples.asJava)
  if (eidosNode.patterns.nonEmpty)
    data.put("patterns", eidosNode.patterns.asJava)
  // opposite: [String]
  eidosNode.oppositeOpt.foreach { opposite =>
    data.put("opposite", opposite)
  }
  // polarity: [1|-1]
  eidosNode.polarityOpt.foreach { polarity =>
    data.put("polarity", polarity)
  }
  // semantic type: [entity | event | property]
  eidosNode.semanticTypeOpt.foreach { semanticType =>
    data.put("semantic type", semanticType)
  }

  def addChild(localNode: LocalNode): Unit = {
    if (!data.containsKey("children"))
      data.put("children", children)
    children.add(localNode)
  }
}
