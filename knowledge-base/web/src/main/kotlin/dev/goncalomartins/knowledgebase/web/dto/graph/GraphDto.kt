package dev.goncalomartins.knowledgebase.web.dto.graph

import dev.goncalomartins.knowledgebase.common.model.graph.Edge
import dev.goncalomartins.knowledgebase.common.model.graph.Graph
import dev.goncalomartins.knowledgebase.common.model.graph.Node

data class GraphDto(
    val nodes: Set<NodeDto> = setOf(),
    val edges: Set<EdgeDto> = setOf()
)

fun Graph.toDto() = GraphDto(nodes = nodes().map(Node::toDto).toSet(), edges().map(Edge::toDto).toSet())
