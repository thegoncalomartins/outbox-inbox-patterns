package dev.goncalomartins.knowledgebase.common.model.graph

class Graph {
    private var total: Long = 0
    private val nodes: MutableSet<Node> = mutableSetOf()
    private val edges: MutableSet<Edge> = mutableSetOf()

    fun addNode(node: Node) = nodes.add(node)

    fun addEdge(edge: Edge) = edges.add(edge)

    fun nodes(): MutableSet<Node> = nodes

    fun edges(): MutableSet<Edge> = edges

    fun total(): Long = total

    fun total(total: Long): Graph = run {
        this.total = total
        this
    }

    fun isEmpty() = nodes.isEmpty()
}
