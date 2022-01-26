package dev.goncalomartins.knowledgebase.common.service

import dev.goncalomartins.knowledgebase.common.model.graph.Graph
import dev.goncalomartins.knowledge-base.common.model.person.People
import dev.goncalomartins.knowledge-base.common.model.person.Person
import dev.goncalomartins.knowledge-base.common.repository.PersonRepository
import dev.goncalomartins.knowledge-base.common.util.DatabaseUtils
import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.opentracing.Traced
import org.neo4j.driver.reactive.RxTransaction
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
@Traced
class PersonService(val personRepository: PersonRepository, val databaseUtils: DatabaseUtils) {
    companion object {
        const val DEFAULT_LIMIT = 20
        const val DEFAULT_SKIP = 0
    }

    fun findOne(id: String, skip: Int = DEFAULT_SKIP, limit: Int = DEFAULT_LIMIT): Uni<dev.goncalomartins.knowledgebase.common.model.graph.Graph> =
        databaseUtils.inTransaction { transaction ->
            personRepository.findOne(transaction, id, skip, limit)
        }

    fun findAll(skip: Int = DEFAULT_SKIP, limit: Int = DEFAULT_LIMIT): Uni<People> =
        databaseUtils.inTransaction { transaction ->
            personRepository.count(transaction)
                .flatMap { total ->
                    personRepository.findAll(transaction, skip, limit)
                        .collect()
                        .asList()
                        .map {
                            People(total, it)
                        }
                }
        }

    fun save(transaction: RxTransaction, person: Person): Uni<Void> = personRepository.save(transaction, person)

    fun delete(transaction: RxTransaction, person: Person): Uni<Void> = personRepository.delete(transaction, person)
}
