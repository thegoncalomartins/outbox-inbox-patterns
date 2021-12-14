package dev.goncalomartins.cinematography.web.dto.graph

import dev.goncalomartins.cinematography.common.model.graph.Edge
import dev.goncalomartins.cinematography.common.model.graph.Graph
import dev.goncalomartins.cinematography.common.model.graph.Node

data class GraphDto(
    val nodes: MutableSet<NodeDto> = mutableSetOf(),
    val edges: MutableSet<EdgeDto> = mutableSetOf()
)

fun Graph.toDto() = GraphDto(nodes = nodes().map(Node::toDto).toMutableSet(), edges().map(Edge::toDto).toMutableSet())
