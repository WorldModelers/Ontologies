package org.clulab.wm.ontologies

import org.clulab.linnaeus.model.graph.eidos.EidosNetwork
import org.clulab.linnaeus.model.graph.eidos.EidosNode
import org.clulab.linnaeus.model.io.eidos.EidosReader
import org.scalatest._

import scala.collection.mutable

class TestDomainOntology extends FlatSpec with Matchers {

  def trace(text: Seq[String]): Unit = {
//    println(text)
  }

  def error(text: String): Unit = {
    println(text)
  }

  def hasDuplicatePaths(network: EidosNetwork): Boolean = {
    val visitor = new network.HierarchicalGraphVisitor()
    var paths = List.empty[Seq[String]]
    var path = mutable.Seq.empty[String]
    var duplicate = false

    visitor.foreachNode { (node: EidosNode, depth: Int) =>
      if (network.isLeaf(node)) {
        val newPath = path.slice(0, depth) :+ node.name

        trace(newPath)
        if (paths.contains(newPath)) {
          duplicate = true
          error(s"Duplicate path: $newPath")
        }
        else
          paths = newPath :: paths
      }
      else
        if (depth < path.size)
          path(depth) = node.name
        else
          path = path :+ node.name
      true
    }
    duplicate
  }

  def hasDuplicateLeaves(network: EidosNetwork): Boolean = {
    val visitor = new network.HierarchicalGraphVisitor()
    var leaves = List.empty[String]
    var path = mutable.Seq.empty[String]
    var duplicate = false

    visitor.foreachNode { (node: EidosNode, depth: Int) =>
      if (network.isLeaf(node)) {
        val newPath = path.slice(0, depth) :+ node.name
        val newLeaf = node.name

        trace(newPath)
        if (leaves.contains(newLeaf)) {
          duplicate = true
          error(s"Duplicate leaf: $newPath")
        }
        else
          leaves = newLeaf :: leaves
      }
      else
        if (depth < path.size)
          path(depth) = node.name
        else
          path = path :+ node.name
      true
    }
    duplicate
  }

  def hasSpaces(network: EidosNetwork): Boolean = {
    val visitor = new network.HierarchicalGraphVisitor()
    var path = mutable.Seq.empty[String]
    var spaces = false

    visitor.foreachNode { (node: EidosNode, depth: Int) =>
      val nodeName = Option(node.name).getOrElse("")

      if (nodeName.contains(' ')) {
        val newPath = path.slice(0, depth) :+ nodeName
        spaces = true
        error(s"Spaces: $newPath")
      }

      if (network.isLeaf(node)) {
        if (!nodeName.contains(' ')) { // Otherwise already printed
          val newPath = path.slice(0, depth) :+ nodeName
          trace(newPath)
        }
      }
      else
        if (depth < path.size)
          path(depth) = nodeName
        else
          path = path :+ nodeName
        true
    }
    spaces
  }

  def hasBadPolarity(network: EidosNetwork): Boolean = {
    val visitor = new network.HierarchicalGraphVisitor()
    var path = mutable.Seq.empty[String]
    var badPolarity = false

    visitor.foreachNode { (node: EidosNode, depth: Int) =>
      val nodeName = node.name

      if (network.isLeaf(node)) {
        val polarityOpt = node.polarityOpt
        val newPath = path.slice(0, depth) :+ nodeName

        if (polarityOpt.nonEmpty) {
          val polarity = polarityOpt.get

          if (polarity != 1 && polarity != -1) {
            badPolarity = true
            error(s"Invalid polarity '$polarity': $newPath")
          }
          else
            trace(newPath)
        }
        else
          trace(newPath)
      }
      else
        if (depth < path.size)
          path(depth) = nodeName
        else
          path = path :+ nodeName
      true
    }
    badPolarity
  }

  def hasBadSemanticType(network: EidosNetwork): Boolean = {
    val semanticTypes = Array("entity", "event", "property")
    val visitor = new network.HierarchicalGraphVisitor()
    var path = mutable.Seq.empty[String]
    var badSemanticType = false

    visitor.foreachNode { (node: EidosNode, depth: Int) =>
      val nodeName = node.name

      if (network.isLeaf(node)) {
        val semanticTypeOpt = node.semanticTypeOpt
        val newPath = path.slice(0, depth) :+ nodeName

        if (semanticTypeOpt.nonEmpty) {
          val semanticType = semanticTypeOpt.get

          if (!semanticTypes.contains(semanticType)) {
            badSemanticType = true
            error(s"Invalid semantic type '$semanticType': $newPath")
          }
          else
            trace(newPath)
        }
        else
          trace(newPath)
      }
      else
        if (depth < path.size)
          path(depth) = nodeName
        else
          path = path :+ nodeName
      true
    }
    badSemanticType
  }

  def hasUnlabeled(network: EidosNetwork): Boolean = {
    val visitor = new network.HierarchicalGraphVisitor()
    var path = mutable.Seq.empty[String]
    var unlabeled = false

    visitor.foreachNode { (node: EidosNode, depth: Int) =>
      val nodeName = Option(node.name).getOrElse("")

      if (nodeName.isEmpty) {
        val newPath = path.slice(0, depth) :+ nodeName
        unlabeled = true
        error(s"Unlabeled: $newPath")
      }

      if (network.isLeaf(node)) {
        if (nodeName.nonEmpty) { // Otherwise already printed
          val newPath = path.slice(0, depth) :+ nodeName
          trace(newPath)
        }
      }
      else
        if (depth < path.size)
          path(depth) = nodeName
        else
          path = path :+ nodeName
      true
    }
    unlabeled
  }

  def hasMissingOpposites(network: EidosNetwork): Boolean = {
    val visitor = new network.HierarchicalGraphVisitor()
    var leaves: Map[String, EidosNode] = Map.empty
    var path = mutable.Seq.empty[String]
    var missing = false

    visitor.foreachNode { (node: EidosNode, depth: Int) =>
      if (network.isLeaf(node)) {
        val newPath = path.slice(0, depth) :+ node.name

        trace(newPath)
        leaves = leaves + (newPath.mkString("/") -> node)
      }
      else
      if (depth < path.size)
        path(depth) = node.name
      else
        path = path :+ node.name
      true
    }

    leaves.foreach { case (path, node) =>
      if (node.oppositeOpt.isDefined) {
        val opposite = node.oppositeOpt.get

        if (!leaves.contains(opposite)) {
          missing = true
          error(s"Missing opposite: $path -> $opposite")
        }
        else {
          val counterpart = leaves(opposite)

          if (node.polarityOpt.isEmpty || counterpart.polarityOpt.isEmpty) {
            missing = true
            error(s"Missing polarity: $path -> $opposite")
          }
          else {
            val nodePolarity: Int = node.polarityOpt.get
            val counterpartPolarity: Int = counterpart.polarityOpt.get

            if (nodePolarity != -counterpartPolarity) {
              missing = true
              error(s"Mismatched polarity: $path -> $opposite")
            }
          }
        }
      }
    }
    missing
  }

  def hasMissingExamples(network: EidosNetwork): Boolean = {
    val visitor = new network.HierarchicalGraphVisitor()
    var path = mutable.Seq.empty[String]
    var missing = false

    visitor.foreachNode { (node: EidosNode, depth: Int) =>
      val nodeName = Option(node.name).getOrElse("")

      if (network.isLeaf(node)) {
        val newPath = path.slice(0, depth) :+ nodeName
        val examples = node.examples
        val isMissing = /*examples.isEmpty ||*/ examples.exists { example =>
          Option(example).isEmpty || example.isEmpty
        }
        if (isMissing) {
          error(s"Missing example: $newPath")
          missing = isMissing
        }
        else
          trace(newPath)
      }
      else
        if (depth < path.size)
          path(depth) = nodeName
        else
          path = path :+ nodeName
      true
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
  }

  Seq(
    "./CompositionalOntology_metadata.yml",
    "./wm_flat_metadata.yml"
  ).foreach(test)
}
