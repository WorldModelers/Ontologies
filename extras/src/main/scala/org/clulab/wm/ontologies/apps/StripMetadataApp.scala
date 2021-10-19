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
    val network = new EidosNetwork()
    val reader = new EidosReader(network)

    reader.readFromFile(inputFilename)

    val eidosToLocalMap = new JIdentityHashMap[EidosNode, MetadatalessNode]()

    def getOrAddNode(eidosNode: EidosNode): MetadatalessNode = {
      Option(eidosToLocalMap.get(eidosNode)).getOrElse {
        val localNode = new MetadatalessNode(eidosNode)

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

    val metadatalessNode: MetadatalessNode = eidosToLocalMap.get(network.getRootNode.get)
    val metadatalessNodes = new JArrayList[MetadatalessNode]()
    metadatalessNodes.add(metadatalessNode)

    // See https://stackoverflow.com/questions/57728245/how-can-i-control-yaml-indentation-using-snakeyaml-during-dumping
    val dumperOptions = {
      val dumperOptions = new DumperOptions()
      dumperOptions.setWidth(300)
      dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
      dumperOptions.setIndent(2)
//      dumperOptions.setIndicatorIndent(2)
//      dumperOptions.setIndentWithIndicator(true) // Would like this to be false, but then have indent on that line be 6
      dumperOptions
    }
    val yaml: String = new Yaml(dumperOptions).dumpAs(metadatalessNodes, Tag.SEQ, FlowStyle.BLOCK)

    println(yaml)

    newPrintWriterFromFile(new File(outputFilename)).autoClose { printWriter =>
      printWriter.println(yaml)
    }
  }

  val inputFile = args.lift(0).getOrElse(syntax())
  val outputFile = args.lift(1).getOrElse(syntax())

  stripMetadata(inputFile, outputFile)
}

class MetadatalessNode(eidosNode: EidosNode) extends JLinkedHashMap[String, Any] {
//  val node = new JLinkedHashMap[String, Any] // turn this to just name?
  val children = new JArrayList[MetadatalessNode]() // put in names of children, no just node self?

  this.put(eidosNode.name, children)

  def addChild(localNode: MetadatalessNode): Unit = {
//    if (!node.containsKey("children"))
//      node.put("children", children)
    children.add(localNode)
  }
}
