package dev.goncalomartins.cinematography.web.dto.graph

import dev.goncalomartins.cinematography.common.model.graph.Node

data class NodeDto(val id: Long, val label: String, val metadata: Any)

fun Node.toDto() = NodeDto(
    id = id,
    label = label,
    metadata = metadata
)
