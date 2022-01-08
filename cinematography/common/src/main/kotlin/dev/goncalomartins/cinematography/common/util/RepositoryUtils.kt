package dev.goncalomartins.cinematography.common.util

import dev.goncalomartins.cinematography.common.model.graph.Edge
import dev.goncalomartins.cinematography.common.model.graph.Graph
import dev.goncalomartins.cinematography.common.model.graph.Node
import org.neo4j.driver.Record
import java.util.function.Function
import org.neo4j.driver.types.Node as Neo4jNode

class RepositoryUtils {
    companion object {
        fun graphPaths(graph: Graph): Function<in Record, out Neo4jNode> = Function { record ->
            val path = if (record["path"].isNull) null else record["path"].asPath()
            val node = record["node"].asNode()

            graph.addNode(Node.fromNeo4jNode(node))

            path?.forEach { segment ->
                graph.addNode(Node.fromNeo4jNode(segment.start()))
                graph.addNode(Node.fromNeo4jNode(segment.end()))
                graph.addEdge(Edge.fromNeo4jRelationship(segment.relationship()))
            }

            node
        }

        fun prepareQuery(query: String, params: Map<String, String>): String {
            var newQuery = query

            params.forEach { (key, value) ->
                newQuery = newQuery.replace("\$$key", value)
            }

            return newQuery
        }
    }
}
