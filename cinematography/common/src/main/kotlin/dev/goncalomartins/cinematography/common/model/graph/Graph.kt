package dev.goncalomartins.cinematography.common.model.graph

class Graph() {
    private val nodes: MutableSet<Node> = mutableSetOf()
    private val edges: MutableSet<Edge> = mutableSetOf()

    fun addNode(node: Node) = nodes.add(node)

    fun addEdge(edge: Edge) = edges.add(edge)
}
