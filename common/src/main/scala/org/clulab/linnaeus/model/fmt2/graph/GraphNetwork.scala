package org.clulab.linnaeus.model.fmt2.graph

import scala.collection.mutable

class GraphNetwork[
  NetworkIdentityType,
  NodeIdentityType, NodeType <: GraphNode[NodeIdentityType],
  EdgeIdentityType, EdgeType <: GraphEdge[EdgeIdentityType]
](val id: NetworkIdentityType)
    extends Identifyable[NetworkIdentityType] {

  case class Branch(source: NodeType, edge: EdgeType, target: NodeType)

  protected[this] class NodePacket(val index: Int, val node: NodeType) extends Ordered[NodePacket] {
    /**
      * Only a small number of edges are expected per node, so they are stored in Seqs
      * rather than Maps.
      */
    var incoming: Seq[EdgePacket] = Seq.empty
    var outgoing: Seq[EdgePacket] = Seq.empty

    def isRoot: Boolean = incoming.isEmpty && outgoing.nonEmpty

    def isCrook: Boolean = incoming.nonEmpty && outgoing.nonEmpty

    def isLeaf: Boolean = incoming.nonEmpty && outgoing.isEmpty

    def isSeed: Boolean = incoming.isEmpty && outgoing.isEmpty

    def isUnparented: Boolean = incoming.size == 1

    def isSingleParented: Boolean = incoming.size == 1

    def isMultiParented: Boolean = incoming.size > 1

    override def compare(that: NodePacket): Int = this.index - that.index

    /**
      * These are kept in "linear" order as received.
      */
    def addIncoming(edgePacket: EdgePacket): Unit = incoming = incoming :+ edgePacket

    def addOutgoing(edgePacket: EdgePacket): Unit = outgoing = outgoing :+ edgePacket

    def subIncoming(edgePacket: EdgePacket): Unit = {
      incoming = incoming.filter { incoming =>
        incoming.edge.getId != edgePacket.edge.getId
      }
    }

    def subOutgoing(edgePacket: EdgePacket): Unit = {
      outgoing = outgoing.filter { outgoing =>
        outgoing.edge.getId != edgePacket.edge.getId
      }
    }
  }

  protected[this] class EdgePacket(val index: Int, val sourcePacket: NodePacket, val edge: EdgeType, val targetPacket: NodePacket)
      extends Ordered[EdgePacket] {
    // Connect the node.
    sourcePacket.addOutgoing(this)
    targetPacket.addIncoming(this)

    override def compare(that: EdgePacket): Int = this.index - that.index
  }

  trait GraphVisitor {
    def foreachNode(f: NodeType => Boolean): Boolean
    def foreachEdge(f: (NodeType, EdgeType, NodeType) => Boolean): Boolean
  }

  class ValueGraphVisitor extends GraphVisitor {

    def foreachNode(f: NodeType => Boolean): Boolean = {
      nodePacketMap.values.forall { nodePacket =>
        f(nodePacket.node)
      }
    }

    def foreachEdge(f: (NodeType, EdgeType, NodeType) => Boolean): Boolean = {
      edgePacketMap.values.forall { edgePacket =>
        f(edgePacket.sourcePacket.node, edgePacket.edge, edgePacket.targetPacket.node)
      }
    }
  }

  class LinearGraphVisitor extends GraphVisitor {
    def foreachNode(f: NodeType => Boolean): Boolean = {
      nodePacketMap.values.toSeq.sorted.forall { nodePacket =>
        f(nodePacket.node)
      }
    }

    def foreachEdge(f: (NodeType, EdgeType, NodeType) => Boolean): Boolean = {
      edgePacketMap.values.toSeq.sorted.forall { edgePacket =>
        f(edgePacket.sourcePacket.node, edgePacket.edge, edgePacket.targetPacket.node)
      }
    }

    def mapNode[T](f: NodeType => T): Seq[T] = {
      nodePacketMap.values.toSeq.sorted.map { nodePacket =>
        f(nodePacket.node)
      }
    }

    def mapEdge[T](f: (NodeType, EdgeType, NodeType) => T): Seq[T] = {
      edgePacketMap.values.toSeq.sorted.map { edgePacket =>
        f(edgePacket.sourcePacket.node, edgePacket.edge, edgePacket.targetPacket.node)
      }
    }
  }

  class HierarchicalGraphVisitor extends GraphVisitor {
    /**
      * This has an implied top-down order that is only suitable for trees.
      * It will recurse infinitely if there are cycles.
      */
    def foreachNode(f: NodeType => Boolean): Boolean = {

      def foreachNode(nodePacket: NodePacket, visited: List[NodeIdentityType]): Boolean = {
        val nodeId = nodePacket.node.getId

        if (!visited.contains(nodeId)) {
          f(nodePacket.node) &&
          nodePacket.outgoing.forall { edgePacket =>
            foreachNode(edgePacket.targetPacket, nodeId :: visited)
          }
        }
        else true
      }

      getRootPackets.toSeq.sorted.forall { root =>
        foreachNode(root, Nil)
      }
    }

    // This one adds depth which is handy for tabbing.  It would be cleaner, but slower,
    // to query each node for its depth.  Consider it.  Additional information that could
    // be communicated is whether or not the node is a leaf.
    def foreachNode(f: (NodeType, Int) => Boolean): Unit = {

      def foreachNode(nodePacket: NodePacket, visited: List[NodeIdentityType]): Unit = {
        val nodeId = nodePacket.node.getId

        if (!visited.contains(nodeId)) {
          f(nodePacket.node, visited.size)
          nodePacket.outgoing.foreach { edgePacket =>
            foreachNode(edgePacket.targetPacket, nodeId :: visited)
          }
        }
      }

      getRootPackets.toSeq.sorted.foreach { root =>
        foreachNode(root, Nil)
      }
    }

    def foreachEdge(f: (NodeType, EdgeType, NodeType) => Boolean): Boolean = {

      def foreachEdge(sourcePacket: NodePacket, visited: List[NodeIdentityType]): Boolean = {
        sourcePacket.outgoing.forall { edgePacket =>
          val nodeId = edgePacket.targetPacket.node.getId

          if (!visited.contains(nodeId)) {
            f(sourcePacket.node, edgePacket.edge, edgePacket.targetPacket.node) &&
                foreachEdge(edgePacket.targetPacket, nodeId :: visited)
          }
          else true
        }
      }

      getRootPackets.toSeq.sorted.forall { root =>
        foreachEdge(root, List(root.node.getId))
      }
    }

    def foldDown[T](initial: T)(f: (T, NodeType) => T): T = {

      def foldDown(current: T, nodePacket: NodePacket, visited: List[NodeIdentityType]): T = {
        val nodeId = nodePacket.node.getId

        if (!visited.contains(nodeId)) {
          val result = f(current, nodePacket.node)
          // This calculates the result here and provides the result to
          // the function used for all the children.
          nodePacket.outgoing.foreach { edgePacket =>
            foldDown(result, edgePacket.targetPacket, nodeId :: visited)
          }
          result
        }
        else current
      }

      foldDown(initial, getRootPacket, Nil)
    }

    def foldUp[T](f: (NodeType, Seq[T]) => T): T = {

      def foldUp(nodePacket: NodePacket, visited: List[NodeIdentityType]): Option[T] = {
        val nodeId = nodePacket.node.getId

        if (!visited.contains(nodeId)) {
          // This calculates the result here and provides the result to
          // the function used for all the children.
          val arguments = nodePacket.outgoing.flatMap { edge =>
            foldUp(edge.targetPacket, nodeId :: visited)
          }
          val result = Some(f(nodePacket.node, arguments))

          result
        }
        else None
      }

      foldUp(getRootPacket, Nil).get
    }
  }

  /**
    * The IdentityTypes here are used to disambiguate the nodes and edges and are suitable map keys.
    */
  protected var nodePacketMap: mutable.Map[NodeIdentityType, NodePacket] = mutable.Map.empty
  protected var edgePacketMap: mutable.Map[EdgeIdentityType, EdgePacket] = mutable.Map.empty
  /**
    * These Indexers allow the nodes and edges to be sorted in the order added (newed).
    * This is especially useful when rewriting a network so that before and after can be compared.
    */
  protected val nodePacketIndexer: Indexer = new Indexer()
  protected val edgePacketIndexer: Indexer = new Indexer()

  def getId: NetworkIdentityType = id

  // Return true if node was added, false if a duplicate, for example.
  def addNode(node: NodeType): Boolean = {
    if (nodePacketMap.contains(node.getId))
      false
    else {
      val nodePacket = new NodePacket(nodePacketIndexer.next, node)
      addNode(nodePacket)
      true
    }
  }

  protected def addNode(nodePacket: NodePacket, nodePacketMap: mutable.Map[NodeIdentityType, NodePacket]): NodePacket = {
    nodePacketMap += nodePacket.node.getId -> nodePacket
    nodePacket
  }

  protected def addNode(nodePacket: NodePacket): NodePacket = addNode(nodePacket, nodePacketMap)

  // Return true if edge was added, false if a duplicate, for example.
  def addEdge(sourceId: NodeIdentityType, edge: EdgeType, targetId: NodeIdentityType): Boolean = {
    if (!nodePacketMap.contains(sourceId) ||
        edgePacketMap.contains(edge.getId) ||
        !nodePacketMap.contains(targetId))
      false
    else {
      addEdge(sourceId, edge, targetId, edgePacketMap)
      true
    }
  }

  protected def addEdge(sourceId: NodeIdentityType, edge: EdgeType, targetId: NodeIdentityType,
      edgePacketMap: mutable.Map[EdgeIdentityType, EdgePacket]): Unit = {
    val sourcePacket = nodePacketMap(sourceId)
    val targetPacket = nodePacketMap(targetId)

    addEdge(sourcePacket, edge, targetPacket, edgePacketMap)
  }

  protected def addEdge(sourcePacket: NodePacket, edge: EdgeType, targetPacket: NodePacket): EdgePacket =
      addEdge(sourcePacket, edge, targetPacket, edgePacketMap)

  protected def addEdge(sourcePacket: NodePacket, edge: EdgeType, targetPacket: NodePacket,
      edgePacketMap: mutable.Map[EdgeIdentityType, EdgePacket]): EdgePacket = {
    val edgePacket = new EdgePacket(edgePacketIndexer.next, sourcePacket, edge, targetPacket)

    edgePacketMap += edge.getId -> edgePacket
    edgePacket
  }

  protected def getRootPackets(nodePacketMap: mutable.Map[NodeIdentityType, NodePacket]): Iterable[NodePacket] =
      nodePacketMap.values.filter(_.isRoot)

  protected def getRootPackets: Iterable[NodePacket] = getRootPackets(nodePacketMap)

  protected def getRootPacket: NodePacket = {
    val rootPackets = getRootPackets.toSeq

    require(rootPackets.size == 1)
    rootPackets.head
  }

  def getRootNode: Option[NodeType] = {
    val rootNodes = getRootNodes

    if (rootNodes.size == 1) Some(rootNodes.head)
    else None
  }

  def getRootNodes: Seq[NodeType] = getRootPackets.map { nodePacket => nodePacket.node }.toSeq

  def newRootNode(): NodeType = ???

  def newRootEdge(): EdgeType = ???

  def reroot(): Unit = {
    val oldRootPackets = getRootPackets
    val rootPacket = new NodePacket(nodePacketIndexer.next, newRootNode())

    addNode(rootPacket)
    oldRootPackets.foreach { oldRootPacket =>
      addEdge(rootPacket, newRootEdge(), oldRootPacket)
    }
  }

  def isLeaf(node: NodeType): Boolean = {
    nodePacketMap(node.getId).isLeaf
  }

  def isTree: Boolean = false

  def getPrunedNodes: Seq[NodeType] = {
    var visitedCounts: mutable.Map[NodeIdentityType, Int] = mutable.Map.empty

    new ValueGraphVisitor().foreachNode { node =>
      visitedCounts += node.getId -> 0
      true
    }
    new HierarchicalGraphVisitor().foreachNode { node =>
      visitedCounts(node.getId) = visitedCounts(node.getId) + 1
      true
    }

    val unvisitedCounts = visitedCounts.filter { case (key, value) =>
      value == 0
    }
    val result = unvisitedCounts.map { case (key, value) =>
      nodePacketMap(key).node
    }.toSeq

    result
  }

  def getIngrownBranches: Seq[Branch] = {
    var visitedCounts: mutable.Map[EdgeIdentityType, Int] = mutable.Map.empty

    new ValueGraphVisitor().foreachEdge { (source, edge, target) =>
      visitedCounts += edge.getId -> 0
      true
    }

    new HierarchicalGraphVisitor().foreachEdge { (_, edge, _) =>
      visitedCounts(edge.getId) = visitedCounts(edge.getId) + 1
      true
    }

    val unvisitedCounts = visitedCounts.filter { case (key, value) =>
      value == 0
    }
    val result = unvisitedCounts.map { case (key, value) =>
      val edgePacket = edgePacketMap(key)

      new Branch(edgePacket.sourcePacket.node, edgePacket.edge, edgePacket.targetPacket.node)
    }.toSeq

    result
  }

  def getMultiParentedNodes(): Seq[NodeType] =
      nodePacketMap.values.filter(_.isMultiParented).map { nodePacket => nodePacket.node }.toSeq
}
