package dev.goncalomartins.knowledgebase.web.dto.graph

import dev.goncalomartins.knowledgebase.common.model.graph.Node

data class NodeDto(val id: Long, val label: String, val metadata: Any)

fun Node.toDto() = NodeDto(
    id = id,
    label = label,
    metadata = metadata
)
