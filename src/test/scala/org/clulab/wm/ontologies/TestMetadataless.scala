package org.clulab.wm.ontologies

import org.clulab.linnaeus.model.graph.eidos.EidosNetwork
import org.clulab.linnaeus.model.graph.eidos.EidosNoMetaNetwork
import org.clulab.linnaeus.model.graph.eidos.EidosNoMetaNode
import org.clulab.linnaeus.model.graph.eidos.EidosNode
import org.clulab.linnaeus.model.io.eidos.EidosNoMetaReader
import org.clulab.linnaeus.model.io.eidos.EidosReader
import org.scalatest._

import scala.collection.mutable.ArrayBuffer

class TestMetadataless extends FlatSpec with Matchers {

  def test(metadataMorePath: String, metadataLessPath: String): Unit = {
    val moreNetwork = new EidosNetwork()
    val moreReader = new EidosReader(moreNetwork)

    val lessNetwork = new EidosNoMetaNetwork()
    val lessReader = new EidosNoMetaReader(lessNetwork)

    behavior of s"$metadataMorePath and $metadataLessPath"

    it should "load with metadata without exception" in {
      moreReader.readFromFile(metadataMorePath)
    }

    it should "load without metadata without exception" in {
      lessReader.readFromFile(metadataLessPath)
    }

    // This shows that all the nodes were read with matching names in the same order.
    it should "have the same nodes" in {
      val moreNodes = {
        val visitor = new moreNetwork.LinearGraphVisitor()
        val arrayBuffer = new ArrayBuffer[String]()

        visitor.foreachNode { node: EidosNode =>
          val nodeName = Option(node.name).getOrElse("")
          arrayBuffer += nodeName
          true
        }
        arrayBuffer.toArray
      }.mkString("\n")
      val lessNodes = {
        val visitor = new lessNetwork.LinearGraphVisitor()
        val arrayBuffer = new ArrayBuffer[String]()

        visitor.foreachNode { node: EidosNoMetaNode =>
          val nodeName = Option(node.name).getOrElse("")
          arrayBuffer += nodeName
          true
        }
        arrayBuffer.toArray
      }.mkString("\n")

      moreNodes should be (lessNodes)
    }

    it should "have the same edges" in {
      val moreEdges = {
        val visitor = new moreNetwork.LinearGraphVisitor()
        val arrayBuffer = new ArrayBuffer[String]()

        visitor.foreachEdge { (fromNode: EidosNode, _, toNode: EidosNode) =>
          val fromNodeName = Option(fromNode.name).getOrElse("")
          val toNodeName = Option(toNode.name).getOrElse("")
          arrayBuffer += s"$fromNodeName -> $toNodeName"
          true
        }
        arrayBuffer.toArray
      }.mkString("\n")
      val lessEdges = {
        val visitor = new lessNetwork.LinearGraphVisitor()
        val arrayBuffer = new ArrayBuffer[String]()

        visitor.foreachEdge { (fromNode: EidosNoMetaNode, _, toNode: EidosNoMetaNode) =>
          val fromNodeName = Option(fromNode.name).getOrElse("")
          val toNodeName = Option(toNode.name).getOrElse("")
          arrayBuffer += s"$fromNodeName -> $toNodeName"
          true
        }
        arrayBuffer.toArray
      }.mkString("\n")

      moreEdges should be (lessEdges)
    }
  }

  Seq(
    ("./CompositionalOntology_metadata.yml", "./CompositionalOntology.yml"),
    ("./wm_flat_metadata.yml", "./wm_flat.yml")
  ).foreach { pair => test(pair._1, pair._2) }
}
