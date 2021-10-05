package org.clulab.wm.ontologies.apps

import org.clulab.linnaeus.model.graph.eidos.EidosNetwork
import org.clulab.linnaeus.model.graph.eidos.EidosNode
import org.clulab.linnaeus.model.io.eidos.EidosReader
import org.clulab.wm.eidos.utils.FileUtils
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.DumperOptions.FlowStyle
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.nodes.Tag

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import scala.collection.JavaConverters._
import java.util.{ArrayList => JArrayList}
import java.util.{IdentityHashMap => JIdentityHashMap}
import java.util.{LinkedHashMap => JLinkedHashMap}

class Converter(inputFile: String, outputFilename: String, newLocalNode: EidosNode => LocalNode) {
  val utf8: String = StandardCharsets.UTF_8.toString

  def newPrintWriterFromFile(file: File): PrintWriter =
      new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), utf8))

  val (headerText, network) = {
    val network = new EidosNetwork()
    val reader = new EidosReader(network)
    val text = FileUtils.getTextFromFile(inputFile)
    val lines = text.split('\n')
    val headers = lines.takeWhile(_.startsWith("#"))
    val trailers = lines
        .drop(headers.length)
        .filter(!_.trim.startsWith("#")) // Do not accept complete comment lines.
        // The lines starting a list can't use this trick.  Comments will remain.
        .map(line => if (line.trim.startsWith("-")) line else line.replace('#', '@'))

    val trailerText = trailers.mkString("\n")

    reader.readFromString(trailerText)
    (headers.mkString("", "\n", if (headers.isEmpty) "" else "\n"), network)
  }
  val eidosToLocalMap = new JIdentityHashMap[EidosNode, LocalNode]()

  def getOrAddNode(eidosNode: EidosNode): LocalNode = {
    Option(eidosToLocalMap.get(eidosNode)).getOrElse {
      val localNode = newLocalNode(eidosNode)

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
  // See https://stackoverflow.com/questions/57728245/how-can-i-control-yaml-indentation-using-snakeyaml-during-dumping
  val dumperOptions = new DumperOptions()
  dumperOptions.setWidth(300)
  dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
  dumperOptions.setIndent(2)
  dumperOptions.setIndicatorIndent(2)
  dumperOptions.setIndentWithIndicator(true) // Would like this to be false, but then have indent on that line be 6
  val yaml = new Yaml(dumperOptions).dumpAs(localRoot, Tag.MAP, FlowStyle.BLOCK)

  def leadingSpaces(text: String): Int = text.indexWhere(_ != ' ')

  val lines = yaml.split('\n')
  val indents = lines.map(leadingSpaces)
  val indentMap = indents
      .distinct
      .sorted
      .zipWithIndex.map { case (indent, index) =>
    indent -> 4 * index
  }
      .toMap
  val newLines = lines.zip(indents).map { case (line, indent) =>
    " " * indentMap(indent) + line.substring(indent).replace('@', '#')
  }
  val newYaml = headerText + newLines.mkString("", "\n", "\n")

  val printWriter = newPrintWriterFromFile(new File(outputFilename))
  printWriter.println(newYaml)
  printWriter.close()
}

object ConverterApp extends App {

  def run(inputFile: String): Unit = {
    new Converter(inputFile, inputFile + ".1", eidosNode => new LocalNode1(eidosNode)) // make children explicit
    new Converter(inputFile, inputFile + ".2", eidosNode => new LocalNode2(eidosNode)) // change OntologyNode to node
    new Converter(inputFile, inputFile + ".3", eidosNode => new LocalNode3(eidosNode)) // move name to top
    // restore comments
  }

  run("./CompositionalOntology_metadata.yml")
  run("./wm_flat_metadata.yml")
}

abstract class LocalNode(eidosNode: EidosNode) extends JLinkedHashMap[String, Any] {
  val node = new JLinkedHashMap[String, Any]
  val children = new JArrayList[LocalNode]()

  def addChild(localNode: LocalNode): Unit = {
    if (!node.containsKey("children"))
      node.put("children", children)
    children.add(localNode)
  }
}

class LocalNode1(eidosNode: EidosNode) extends LocalNode(eidosNode) {
//  this.put("node", node)
  this.put("OntologyNode", node)
  eidosNode.definitionOpt.foreach { definition =>
    node.put("definition", definition)
  }
  if (eidosNode.descriptions.nonEmpty)
    node.put("descriptions", eidosNode.descriptions.asJava)
  // For flat, patterns come before examples and name.  Otherwise, put it after examples.
  if (eidosNode.patterns.nonEmpty)
    node.put("patterns", eidosNode.patterns.asJava) // change back to patterns
  if (eidosNode.examples.nonEmpty)
    node.put("examples", eidosNode.examples.asJava)
  // TODO: Move back up to top
  node.put("name", eidosNode.name) // required
  // opposite: [String]
  eidosNode.oppositeOpt.foreach { opposite =>
    node.put("opposite", opposite)
  }
  // polarity: [1|-1]
  eidosNode.polarityOpt.foreach { polarity =>
    node.put("polarity", polarity)
  }
  // semantic type: [entity | event | property]
  eidosNode.semanticTypeOpt.foreach { semanticType =>
    node.put("semantic type", semanticType)
  }
}

class LocalNode2(eidosNode: EidosNode) extends LocalNode(eidosNode) {
    this.put("node", node)
//  this.put("OntologyNode", node)
  eidosNode.definitionOpt.foreach { definition =>
    node.put("definition", definition)
  }
  if (eidosNode.descriptions.nonEmpty)
    node.put("descriptions", eidosNode.descriptions.asJava)
  // For flat, patterns come before examples and name.  Otherwise, put it after examples.
  if (eidosNode.patterns.nonEmpty)
    node.put("patterns", eidosNode.patterns.asJava) // change back to patterns
  if (eidosNode.examples.nonEmpty)
    node.put("examples", eidosNode.examples.asJava)
  // TODO: Move back up to top
  node.put("name", eidosNode.name) // required
  // opposite: [String]
  eidosNode.oppositeOpt.foreach { opposite =>
    node.put("opposite", opposite)
  }
  // polarity: [1|-1]
  eidosNode.polarityOpt.foreach { polarity =>
    node.put("polarity", polarity)
  }
  // semantic type: [entity | event | property]
  eidosNode.semanticTypeOpt.foreach { semanticType =>
    node.put("semantic type", semanticType)
  }
}

class LocalNode3(eidosNode: EidosNode) extends LocalNode(eidosNode) {
  this.put("node", node)
  //  this.put("OntologyNode", node)
  // TODO: Move back up to top
  node.put("name", eidosNode.name) // required
  eidosNode.definitionOpt.foreach { definition =>
    node.put("definition", definition)
  }
  if (eidosNode.descriptions.nonEmpty)
    node.put("descriptions", eidosNode.descriptions.asJava)
  // For flat, patterns come before examples and name.  Otherwise, put it after examples.
  if (eidosNode.patterns.nonEmpty)
    node.put("patterns", eidosNode.patterns.asJava) // change back to patterns
  if (eidosNode.examples.nonEmpty)
    node.put("examples", eidosNode.examples.asJava)
  // opposite: [String]
  eidosNode.oppositeOpt.foreach { opposite =>
    node.put("opposite", opposite)
  }
  // polarity: [1|-1]
  eidosNode.polarityOpt.foreach { polarity =>
    node.put("polarity", polarity)
  }
  // semantic type: [entity | event | property]
  eidosNode.semanticTypeOpt.foreach { semanticType =>
    node.put("semantic type", semanticType)
  }
}
