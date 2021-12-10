package dev.goncalomartins.cinematography.common.repository

import dev.goncalomartins.cinematography.common.model.graph.Graph
import dev.goncalomartins.cinematography.common.model.person.Person
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
class PersonRepository {
    private companion object {
        const val FIND_ONE_QUERY =
            """
                MATCH path=(p: Person {id: ${'$'}id})-[]-(m: Movie)
                RETURN path ORDER BY p.updated_at DESC SKIP ${'$'}skip LIMIT ${'$'}limit
            """

        const val COUNT_QUERY = """
            MATCH (person: Person)
            RETURN count(person) as count
        """

        const val FIND_ALL_QUERY =
            """
                MATCH (person: Person)
                RETURN person ORDER BY person.updated_at DESC SKIP ${'$'}skip LIMIT ${'$'}limit
            """

        const val CREATE_UPDATE_QUERY =
            """
                MERGE (p:Person {id: ${'$'}id}) 
                ON CREATE SET p.name = ${'$'}name, 
                p.birth_date = ${'$'}birth_date, 
                p.created_at = ${'$'}created_at, 
                p.updated_at = ${'$'}updated_at 
                ON MATCH SET p.name = ${'$'}name, 
                p.birth_date = ${'$'}birth_date, 
                p.created_at = ${'$'}created_at, 
                p.updated_at = ${'$'}updated_at
            """

        const val DELETE_QUERY =
            """
                MATCH (p:Person) 
                WHERE p.id = ${'$'}id
                DETACH DELETE p
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
                            FIND_ONE_QUERY,
                            mapOf(
                                "id" to id,
                                "skip" to skip.toString(),
                                "limit" to limit.toString()
                            )
                        ).records()
                    )
                    .map(RepositoryUtils.graphPaths(graph))
                    .collect()
                    .asList()
                    .map { graph }
            }

    fun findAll(transaction: RxTransaction, skip: Int, limit: Int): Multi<Person> =
        Multi
            .createFrom()
            .publisher(
                transaction.run(
                    FIND_ALL_QUERY,
                    mapOf(
                        "skip" to skip.toString(),
                        "limit" to limit.toString()
                    )
                ).records()
            ).map { record -> Person.fromNode(record["person"].asNode()) }

    fun count(transaction: RxTransaction): Uni<Long> =
        Uni
            .createFrom()
            .publisher(
                transaction.run(
                    COUNT_QUERY
                ).records()
            ).map { record -> record["count"].asLong() }

    fun save(transaction: RxTransaction, person: Person): Uni<Void> =
        Uni
            .createFrom()
            .publisher(
                transaction.run(
                    CREATE_UPDATE_QUERY,
                    mapOf(
                        "id" to person.id,
                        "name" to person.name,
                        "birth_date" to person.birthDate,
                        "created_at" to LocalDateTime.ofInstant(person.createdAt, ZoneId.systemDefault()),
                        "updated_at" to LocalDateTime.ofInstant(person.updatedAt, ZoneId.systemDefault()),
                    )
                ).records()
            ).onItem()
            .ignore()
            .andContinueWithNull()

    fun delete(transaction: RxTransaction, person: Person): Uni<Void> =
        Uni
            .createFrom()
            .publisher(
                transaction.run(
                    DELETE_QUERY,
                    mapOf(
                        "id" to person.id
                    )
                ).records()
            ).onItem()
            .ignore()
            .andContinueWithNull()
}
