package dev.goncalomartins.cinematography.common.util

import dev.goncalomartins.cinematography.common.model.graph.Edge
import dev.goncalomartins.cinematography.common.model.graph.Graph
import dev.goncalomartins.cinematography.common.model.graph.Node
import org.neo4j.driver.Record
import org.neo4j.driver.types.Path
import java.util.function.Function

class RepositoryUtils {
    companion object {
        fun graphPaths(graph: Graph): Function<in Record, out Path> = Function { record ->
            val path = record["path"].asPath()

            path.forEach { segment ->
                graph.addNode(Node.fromNeo4jNode(segment.start()))
                graph.addNode(Node.fromNeo4jNode(segment.end()))
                graph.addEdge(Edge.fromNeo4jRelationship(segment.relationship()))
            }

            path
        }
    }
}
