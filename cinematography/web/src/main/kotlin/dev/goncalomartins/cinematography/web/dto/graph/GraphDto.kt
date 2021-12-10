package dev.goncalomartins.cinematography.web.dto.graph

data class GraphDto(
    val nodes: MutableSet<NodeDto> = mutableSetOf(),
    val edges: MutableSet<EdgeDto> = mutableSetOf()
)
