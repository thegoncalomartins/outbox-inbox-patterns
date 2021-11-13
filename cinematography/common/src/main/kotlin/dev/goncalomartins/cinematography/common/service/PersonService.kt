package dev.goncalomartins.cinematography.common.service

import dev.goncalomartins.cinematography.common.model.person.Person
import dev.goncalomartins.cinematography.common.repository.PersonRepository
import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.opentracing.Traced
import org.neo4j.driver.reactive.RxTransaction
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
@Traced
class PersonService(val personRepository: PersonRepository) {
    fun save(transaction: RxTransaction, person: Person): Uni<Void> = personRepository.save(transaction, person)

    fun delete(transaction: RxTransaction, person: Person): Uni<Void> = personRepository.delete(transaction, person)
}
