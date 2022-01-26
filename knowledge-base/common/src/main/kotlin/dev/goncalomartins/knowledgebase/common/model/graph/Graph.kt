package dev.goncalomartins.knowledgebase.common.model.graph

class Graph {
    private var total: Long = 0
    private val nodes: MutableSet<dev.goncalomartins.knowledgebase.common.model.graph.Node> = mutableSetOf()
    private val edges: MutableSet<dev.goncalomartins.knowledgebase.common.model.graph.Edge> = mutableSetOf()

    fun addNode(node: dev.goncalomartins.knowledgebase.common.model.graph.Node) = nodes.add(node)

    fun addEdge(edge: dev.goncalomartins.knowledgebase.common.model.graph.Edge) = edges.add(edge)

    fun nodes(): MutableSet<dev.goncalomartins.knowledgebase.common.model.graph.Node> = nodes

    fun edges(): MutableSet<dev.goncalomartins.knowledgebase.common.model.graph.Edge> = edges

    fun total(): Long = total

    fun total(total: Long): dev.goncalomartins.knowledgebase.common.model.graph.Graph = run {
        this.total = total
        this
    }

    fun isEmpty() = nodes.isEmpty()
}
