package org.clulab.wm.ontologies

import org.clulab.linnaeus.model.fmt2.graph.eidos.EidosNetwork
import org.clulab.linnaeus.model.fmt2.graph.eidos.EidosNode
import org.clulab.linnaeus.model.fmt2.io.eidos.EidosReader
import org.scalatest._

import scala.collection.mutable

class TestDomainOntology extends FlatSpec with Matchers {
  type Path = mutable.Seq[String]

  def trace(text: Seq[String]): Unit = {
    //    println(text)
  }

  protected def error(text: String): Boolean = {
    println(text)
    true
  }

  protected val success: Boolean = false

  // If f returns true, it is tracked in overallResult and returned.
  protected def visit(network: EidosNetwork)(f: (EidosNode, Path) => Boolean): Boolean = {
    val visitor = new network.HierarchicalGraphVisitor()
    var path = mutable.Seq.empty[String]
    var overallResult = false

    visitor.foreachNode { (node: EidosNode, depth: Int) =>
      if (network.isLeaf(node)) {
        val newPath = path.slice(0, depth) :+ node.name

        trace(path)
        if (f(node, newPath))
          overallResult = true
      }
      else
        if (depth < path.size)
          path(depth) = node.name
        else
          path = path :+ node.name
      true
    }
    overallResult
  }

  def hasDuplicatePaths(network: EidosNetwork): Boolean = {
    var paths = List.empty[Seq[String]]
    val duplicate = visit(network) { (node: EidosNode, path: Path) =>
      if (paths.contains(path))
        error(s"Duplicate path: $path")
      else {
        paths = path :: paths
        success
      }
    }

    duplicate
  }

  def hasDuplicateLeaves(network: EidosNetwork): Boolean = {
    var leaves = List.empty[String]
    val duplicate = visit(network) { (node: EidosNode, path: Path) =>
      val leaf = node.name

      if (leaves.contains(leaf))
        error(s"Duplicate leaf: $path")
      else {
        leaves = leaf :: leaves
        success
      }
    }

    duplicate
  }

  def hasSpaces(network: EidosNetwork): Boolean = {
    val spaces = visit(network) { (node: EidosNode, path: Path) =>
      val nodeName = Option(node.name).getOrElse("")

      if (nodeName.contains(' ') || path.exists(_.contains(' ')))
        error(s"Spaces: $path")
      else
        success
    }

    spaces
  }

  def hasBadPolarity(network: EidosNetwork): Boolean = {
    val badPolarity = visit(network) { (node: EidosNode, path: Path) =>
      val polarityOpt = node.polarityOpt

      if (polarityOpt.nonEmpty) {
        val polarity = polarityOpt.get

        if (polarity != 1 && polarity != -1)
          error(s"Invalid polarity '$polarity': $path")
        else
          success
      }
      else
        success
    }

    badPolarity
  }

  def hasBadSemanticType(network: EidosNetwork): Boolean = {
    val semanticTypes = Array("entity", "event", "property")
    val badSemanticType = visit(network) { (node: EidosNode, path: Path) =>
      val semanticTypeOpt = node.semanticTypeOpt

      if (semanticTypeOpt.nonEmpty) {
        val semanticType = semanticTypeOpt.get

        if (!semanticTypes.contains(semanticType))
          error(s"Invalid semantic type '$semanticType': $path")
        else
          success
      }
      else
        success
    }

    badSemanticType
  }

  def hasUnknownKeys(network: EidosNetwork): Boolean = {
    val unknownKeys = visit(network) { (node: EidosNode, path: Path) =>
      val otherKeys = node.others

      if (otherKeys.nonEmpty)
        error(s"Unknown keys '${otherKeys.mkString(", ")}': $path")
      else
        success
    }

    unknownKeys
  }

  def hasUnlabeled(network: EidosNetwork): Boolean = {
    val unlabeled = visit(network) { (node: EidosNode, path: Path) =>
      val nodeName = Option(node.name).getOrElse("")

      if (nodeName.isEmpty)
        error(s"Unlabeled: $path")
      else
        success
    }

    unlabeled
  }

  def hasMissingOpposites(network: EidosNetwork): Boolean = {
    var leaves: Map[String, EidosNode] = Map.empty

    visit(network) { (node: EidosNode, path: Path) =>
      leaves = leaves + (path.mkString("/") -> node)
      success
    }

    var missing = false

    leaves.foreach { case (path, node) =>
      if (node.oppositeOpt.isDefined) {
        val opposite = node.oppositeOpt.get

        if (!leaves.contains(opposite))
          missing = error(s"Missing opposite: $path -> $opposite")
        else {
          val counterpart = leaves(opposite)

          if (node.polarityOpt.isEmpty || counterpart.polarityOpt.isEmpty)
            missing = error(s"Missing polarity: $path -> $opposite")
          else {
            val nodePolarity: Int = node.polarityOpt.get
            val counterpartPolarity: Int = counterpart.polarityOpt.get

            if (nodePolarity != -counterpartPolarity)
              missing = error(s"Mismatched polarity: $path -> $opposite")
          }
        }
      }
    }
    missing
  }

  def hasMissingExamples(network: EidosNetwork): Boolean = {
    val missing = visit(network) { (node: EidosNode, path: Path) =>
      val examples = node.examples
      val isMissing = /*examples.isEmpty ||*/ examples.exists { example =>
        Option(example).isEmpty || example.isEmpty
      }

      if (isMissing)
        error(s"Missing example: $path")
      else
        success
    }

    missing
  }

  def test(path: String): Unit = {
    val network = new EidosNetwork()
    val reader = new EidosReader(network)

    behavior of "ontology in " + path

    it should "load without exception" in {
      reader.readFromFile(path)
    }

    it should "not have duplicate paths" in {
      hasDuplicatePaths(network) should be (false)
    }

    it should "not have duplicate leaves" in {
      hasDuplicateLeaves(network) should be (false)
    }

    it should "not have unlabeled nodes" in {
      hasUnlabeled(network) should be (false)
    }

    it should "not have spaces" in {
      hasSpaces(network) should be (false)
    }

    it should "not have missing opposites" in {
      hasMissingOpposites(network) should be (false)
    }

    it should "not have missing examples" in {
      hasMissingExamples(network) should be (false)
    }

    it should "have valid polarity values" in {
      hasBadPolarity(network) should be (false)
    }

    it should "have valid semantic types" in {
      hasBadSemanticType(network) should be (false)
    }

    it should "not have unknown keys" in {
      hasUnknownKeys(network) should be (false)
    }
  }

  Seq(
    "./CompositionalOntology_metadata.yml",
    "./wm_flat_metadata.yml"
  ).foreach(test)
}
