package dev.goncalomartins.knowledgebase.common.model.graph

import org.neo4j.driver.types.Relationship

data class Edge(val id: Long, val from: Long, val to: Long, val relationship: String, val metadata: Map<String, Any>) {
    companion object {
        fun fromNeo4jRelationship(relationship: Relationship) =
            Edge(
                relationship.id(),
                relationship.startNodeId(),
                relationship.endNodeId(),
                relationship.type(),
                relationship.asMap()
            )
    }
}
