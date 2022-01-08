package dev.goncalomartins.cinematography.web.dto.graph

import dev.goncalomartins.cinematography.common.model.graph.Edge

data class EdgeDto(val id: Long, val from: Long, val to: Long, val relationship: String, val metadata: Map<String, Any>? = null)

fun Edge.toDto() = EdgeDto(
    id = id,
    from = from,
    to = to,
    relationship = relationship,
    metadata = metadata.ifEmpty { null }
)
