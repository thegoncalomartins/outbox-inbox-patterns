package dev.goncalomartins.knowledgebase.common.model.graph

import dev.goncalomartins.knowledge-base.common.model.movie.Movie
import dev.goncalomartins.knowledge-base.common.model.person.Person
import java.util.function.Function
import org.neo4j.driver.types.Node as Neo4jNode

data class Node(val id: Long, val label: String, val metadata: NodeMetadata) {

    companion object {
        private val map = mapOf(
            "Person" to Function<Neo4jNode, Node> { Node(it.id(), "Person", Person.fromNode(it)) },
            "Movie" to Function<Neo4jNode, Node> { Node(it.id(), "Movie", Movie.fromNode(it)) }
        )

        fun fromNeo4jNode(node: Neo4jNode) = map[node.labels().first()]!!.apply(node)
    }
}
