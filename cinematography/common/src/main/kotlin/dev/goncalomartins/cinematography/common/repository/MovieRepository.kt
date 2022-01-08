package dev.goncalomartins.cinematography.common.repository

import dev.goncalomartins.cinematography.common.model.graph.Graph
import dev.goncalomartins.cinematography.common.model.movie.Movie
import dev.goncalomartins.cinematography.common.util.RepositoryUtils
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.opentracing.Traced
import org.neo4j.driver.reactive.RxTransaction
import java.time.LocalDateTime
import java.time.ZoneId
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
@Traced
class MovieRepository {
    private companion object {
        const val FIND_ONE_QUERY =
            """
                MATCH (node: Movie {id: ${'$'}id})
                OPTIONAL MATCH path=(node)-[]->(p: Person)
                RETURN node, path ORDER BY p.updated_at DESC SKIP ${'$'}skip LIMIT ${'$'}limit
            """

        const val COUNT_ONE_QUERY =
            """
                MATCH (m: Movie {id: ${'$'}id})
                OPTIONAL MATCH path=(m)-[]->(p: Person)
                RETURN CASE WHEN count(p) > count(path) THEN count(p) ELSE count(path) END as count
            """

        const val COUNT_ALL_QUERY =
            """
                MATCH (movie: Movie)
                RETURN count(movie) as count
            """

        const val FIND_ALL_QUERY =
            """
                MATCH (movie: Movie)
                RETURN movie ORDER BY movie.updated_at DESC SKIP ${'$'}skip LIMIT ${'$'}limit
            """

        const val CREATE_UPDATE_QUERY =
            """
                MERGE (m:Movie {id: ${'$'}id}) 
                ON CREATE SET m.title = ${'$'}title, 
                m.released = ${'$'}released, 
                m.created_at = ${'$'}created_at, 
                m.updated_at = ${'$'}updated_at 
                ON MATCH SET m.title = ${'$'}title, 
                m.released = ${'$'}released, 
                m.created_at = ${'$'}created_at, 
                m.updated_at = ${'$'}updated_at
            """

        const val CREATE_DIRECTED_BY_RELATIONSHIP_QUERY =
            """
                MATCH (m:Movie),(p:Person) 
                WHERE m.id = ${'$'}movie_id AND p.id = ${'$'}person_id 
                MERGE (m)-[:DIRECTED_BY]->(p)
            """

        const val CREATE_CAST_RELATIONSHIP_QUERY =
            """
                MATCH (m:Movie),(p:Person) 
                WHERE m.id = ${'$'}movie_id AND p.id = ${'$'}person_id 
                MERGE (m)-[:CAST {role: ${'$'}role}]->(p)
            """

        const val DELETE_RELATIONSHIPS_QUERY =
            """
                MATCH (m:Movie)-[r]->() 
                WHERE m.id = ${'$'}movie_id 
                DELETE r
            """

        const val DELETE_QUERY =
            """
                MATCH (m:Movie) 
                WHERE m.id = ${'$'}id 
                DETACH DELETE m
            """
    }

    fun findOne(transaction: RxTransaction, id: String, skip: Int, limit: Int): Uni<Graph> =
        Uni
            .createFrom()
            .item { Graph() }
            .flatMap { graph ->
                Multi
                    .createFrom()
                    .publisher(
                        transaction.run(
                            RepositoryUtils.prepareQuery(
                                FIND_ONE_QUERY,
                                mapOf("skip" to skip.toString(), "limit" to limit.toString())
                            ),
                            mapOf(
                                "id" to id
                            )
                        ).records()
                    )
                    .map(RepositoryUtils.graphPaths(graph))
                    .collect()
                    .asList()
                    .map { graph }
            }
            .flatMap { graph ->
                countOne(transaction, id)
                    .map { total -> graph.total(total) }
            }

    fun findAll(transaction: RxTransaction, skip: Int, limit: Int): Multi<Movie> =
        Multi
            .createFrom()
            .publisher(
                transaction.run(
                    RepositoryUtils.prepareQuery(
                        FIND_ALL_QUERY,
                        mapOf("skip" to skip.toString(), "limit" to limit.toString())
                    )
                ).records()
            ).map { record -> Movie.fromNode(record["movie"].asNode()) }

    fun count(transaction: RxTransaction): Uni<Long> =
        Uni
            .createFrom()
            .publisher(
                transaction.run(
                    COUNT_ALL_QUERY
                ).records()
            ).map { record -> record["count"].asLong() }

    fun save(transaction: RxTransaction, movie: Movie): Uni<Void> =
        createOrUpdateMovie(transaction, movie)
            .flatMap { deleteMovieRelationships(transaction, movie) }
            .flatMap { createDirectedByRelationships(transaction, movie) }
            .flatMap { createCastRelationships(transaction, movie) }

    fun delete(transaction: RxTransaction, movie: Movie): Uni<Void> =
        Uni
            .createFrom()
            .publisher(
                transaction.run(
                    DELETE_QUERY,
                    mapOf(
                        "id" to movie.id
                    )
                ).records()
            ).onItem()
            .ignore()
            .andContinueWithNull()

    private fun countOne(transaction: RxTransaction, id: String): Uni<Long> =
        Uni
            .createFrom()
            .publisher(
                transaction.run(
                    COUNT_ONE_QUERY,
                    mapOf(
                        "id" to id
                    )
                ).records()
            ).map { record -> record["count"].asLong() }

    private fun createOrUpdateMovie(transaction: RxTransaction, movie: Movie): Uni<Void> =
        Uni
            .createFrom()
            .publisher(
                transaction.run(
                    CREATE_UPDATE_QUERY,
                    mapOf(
                        "id" to movie.id,
                        "title" to movie.title,
                        "released" to movie.released,
                        "created_at" to LocalDateTime.ofInstant(movie.createdAt, ZoneId.systemDefault()),
                        "updated_at" to LocalDateTime.ofInstant(movie.updatedAt, ZoneId.systemDefault()),
                    )
                ).records()
            ).onItem()
            .ignore()
            .andContinueWithNull()

    private fun createDirectedByRelationships(transaction: RxTransaction, movie: Movie): Uni<Void> =
        Multi
            .createFrom()
            .iterable(movie.directedBy)
            .onItem()
            .call { directorId ->
                Uni
                    .createFrom()
                    .publisher(
                        transaction.run(
                            CREATE_DIRECTED_BY_RELATIONSHIP_QUERY,
                            mapOf(
                                "movie_id" to movie.id,
                                "person_id" to directorId
                            )
                        ).records()
                    ).onItem()
                    .ignore()
                    .andContinueWithNull()
            }.collect()
            .asList()
            .onItem()
            .ignore()
            .andContinueWithNull()

    private fun createCastRelationships(transaction: RxTransaction, movie: Movie): Uni<Void> =
        Multi
            .createFrom()
            .iterable(movie.cast)
            .onItem()
            .call { actor ->
                Uni
                    .createFrom()
                    .publisher(
                        transaction.run(
                            CREATE_CAST_RELATIONSHIP_QUERY,
                            mapOf(
                                "movie_id" to movie.id,
                                "person_id" to actor.personId,
                                "role" to actor.role
                            )
                        ).records()
                    ).onItem()
                    .ignore()
                    .andContinueWithNull()
            }.collect()
            .asList()
            .onItem()
            .ignore()
            .andContinueWithNull()

    private fun deleteMovieRelationships(transaction: RxTransaction, movie: Movie): Uni<Void> =
        Uni
            .createFrom()
            .publisher(
                transaction.run(
                    DELETE_RELATIONSHIPS_QUERY,
                    mapOf(
                        "movie_id" to movie.id
                    )
                ).records()
            ).onItem()
            .ignore()
            .andContinueWithNull()
}
