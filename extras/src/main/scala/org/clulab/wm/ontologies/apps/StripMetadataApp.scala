package org.clulab.wm.ontologies.apps

import org.clulab.linnaeus.model.fmt2.graph.eidos.EidosNode
import org.clulab.linnaeus.model.fmt2.graph.eidos.{EidosNetwork, EidosNode}
import org.clulab.linnaeus.model.fmt2.io.eidos.EidosReader
import org.clulab.wm.eidos.utils.Closer.AutoCloser
import org.yaml.snakeyaml.{DumperOptions, Yaml}
import org.yaml.snakeyaml.DumperOptions.FlowStyle
import org.yaml.snakeyaml.nodes.Tag

import java.io.{File, FileOutputStream, OutputStreamWriter, PrintWriter}
import java.nio.charset.StandardCharsets
import java.util.{ArrayList => JArrayList}
import java.util.{IdentityHashMap => JIdentityHashMap}
import java.util.{LinkedHashMap => JLinkedHashMap}
import scala.collection.mutable.ArrayBuffer

object StripMetadataApp extends App {
  val utf8: String = StandardCharsets.UTF_8.toString

  def syntax(): Nothing = {
    println(s"Syntax: ${this.getClass.getName.dropRight(1)} inputFile.yml outputFile.yml")
    System.exit(-1)
    throw new RuntimeException("Syntax error")
  }

  def newPrintWriterFromFile(file: File): PrintWriter =
      new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), utf8))

  def stripMetadata(inputFilename: String, outputFilename: String): Unit = {
    val network = {
      val network = new EidosNetwork()
      val reader = new EidosReader(network)
      reader.readFromFile(inputFilename)
      network
    }
    val parentAndChildNodes = {
      val parentAndChildNodes = new ArrayBuffer[(EidosNode, EidosNode)]()
      new (network.HierarchicalGraphVisitor).foreachEdge { case (eidosParentNode, _, eidosChildNode) =>
        parentAndChildNodes.append((eidosParentNode, eidosChildNode))
        true
      }
      parentAndChildNodes.toArray
    }
    val metadatalessNode: MetadatalessNode = {
      val eidosToLocalMap = new JIdentityHashMap[EidosNode, MetadatalessNode]()

      def getOrAddNode(eidosNode: EidosNode, hasChildren: Boolean): MetadatalessNode = {
        Option(eidosToLocalMap.get(eidosNode)).getOrElse {
          val localNode = new MetadatalessNode(eidosNode, hasChildren)

          eidosToLocalMap.put(eidosNode, localNode)
          localNode
        }
      }

      parentAndChildNodes.foreach { case (eidosParentNode, eidosChildNode) =>
        val localParentNode = getOrAddNode(eidosParentNode, true)
        val childHasChildren = parentAndChildNodes.exists { case (eidosParentNode, _) =>
          eidosParentNode.eq(eidosChildNode)
        }
        if (childHasChildren) {
          val localChildNode = getOrAddNode(eidosChildNode, childHasChildren)
          localParentNode.addChild(localChildNode)
        }
        else
          // This is not even a node then, but just a string.
          localParentNode.addChild(eidosChildNode.name)
      }
      eidosToLocalMap.get(network.getRootNode.get)
    }
    val metadatalessNodes = {
      val metadatalessNodes = new JArrayList[MetadatalessNode]()
      metadatalessNodes.add(metadatalessNode)
      metadatalessNodes
    }
    // See https://stackoverflow.com/questions/57728245/how-can-i-control-yaml-indentation-using-snakeyaml-during-dumping
    val dumperOptions = {
      val dumperOptions = new DumperOptions()
      dumperOptions.setWidth(300)
      dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
      dumperOptions.setIndent(2)
      dumperOptions
    }
    val yaml: String = new Yaml(dumperOptions).dumpAs(metadatalessNodes, Tag.SEQ, FlowStyle.BLOCK)

    newPrintWriterFromFile(new File(outputFilename)).autoClose { printWriter =>
      printWriter.println(yaml)
    }
  }

  val inputFile = args.lift(0).getOrElse(syntax())
  val outputFile = args.lift(1).getOrElse(syntax())

  stripMetadata(inputFile, outputFile)
}

class MetadatalessNode(eidosNode: EidosNode, hasChildren: Boolean) extends JLinkedHashMap[String, Any] {
  val children = new JArrayList[Any]() // either another MetadatalessNode or a String

  if (hasChildren)
    this.put(eidosNode.name, children)

  def addChild(localNode: MetadatalessNode): Unit = {
    children.add(localNode)
  }

  def addChild(child: String): Unit = {
    children.add(child)
  }
}
