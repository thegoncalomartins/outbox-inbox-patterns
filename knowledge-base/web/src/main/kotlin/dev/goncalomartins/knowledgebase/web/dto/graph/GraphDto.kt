package dev.goncalomartins.knowledgebase.web.dto.graph

import dev.goncalomartins.knowledgebase.common.model.graph.Edge
import dev.goncalomartins.knowledgebase.common.model.graph.Graph
import dev.goncalomartins.knowledge-base.common.model.graph.Node

data class GraphDto(
    val nodes: Set<NodeDto> = setOf(),
    val edges: Set<EdgeDto> = setOf()
)

fun dev.goncalomartins.knowledgebase.common.model.graph.Graph.toDto() = GraphDto(nodes = nodes().map(Node::toDto).toSet(), edges().map(
    dev.goncalomartins.knowledgebase.common.model.graph.Edge::toDto).toSet())
