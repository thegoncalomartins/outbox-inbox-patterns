package dev.goncalomartins.cinematography.common.repository

import dev.goncalomartins.cinematography.common.model.person.Person
import io.smallrye.mutiny.Uni
import org.neo4j.driver.reactive.RxTransaction
import java.time.LocalDateTime
import java.time.ZoneId
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class PersonRepository {
    private companion object {
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
