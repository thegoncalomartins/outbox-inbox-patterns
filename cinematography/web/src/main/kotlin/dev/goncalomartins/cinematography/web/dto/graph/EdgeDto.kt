package dev.goncalomartins.cinematography.web.dto.graph

import dev.goncalomartins.cinematography.common.model.graph.Edge

data class EdgeDto(val id: Long, val from: Long, val to: Long, val relationship: String)

fun Edge.toDto() = EdgeDto(
    id = id,
    from = from,
    to = to,
    relationship = relationship
)
