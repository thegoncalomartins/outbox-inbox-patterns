package dev.goncalomartins.people.service

import dev.goncalomartins.people.exception.PersonNotFoundException
import dev.goncalomartins.people.model.outbox.EventType
import dev.goncalomartins.people.model.person.People
import dev.goncalomartins.people.model.person.Person
import dev.goncalomartins.people.repository.PersonRepository
import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.opentracing.Traced
import javax.enterprise.context.ApplicationScoped
import javax.transaction.Transactional

@ApplicationScoped
@Traced
class PersonService(
    val repository: PersonRepository,
    val outboxService: OutboxService
) {

    companion object {
        const val DEFAULT_LIMIT = 20
        const val DEFAULT_SKIP = 0
    }

    @Transactional
    fun create(person: Person): Uni<Person> = repository.persist(person)
        .flatMap { newPerson -> outboxService.emitEvent(EventType.CREATED, newPerson).map { newPerson } }

    @Transactional
    fun update(person: Person): Uni<Person> = repository
        .update(person)
        .onItem()
        .ifNull()
        .failWith(PersonNotFoundException(person.id!!.toHexString()))
        .flatMap { updatedPerson -> outboxService.emitEvent(EventType.UPDATED, updatedPerson).map { updatedPerson } }

    fun findAll(limit: Int = DEFAULT_LIMIT, skip: Int = DEFAULT_SKIP): Uni<People> = repository.count()
        .flatMap { total ->
            repository.findAll(limit = limit, skip = skip).collect().asList()
                .map { people -> People(total = total, people = people) }
        }

    fun findOne(id: String): Uni<Person> =
        repository
            .findOne(id)
            .onItem()
            .ifNull()
            .failWith(PersonNotFoundException(id))
            .map { it!! }

    @Transactional
    fun delete(id: String): Uni<Void> =
        repository
            .delete(id)
            .onItem()
            .ifNull()
            .failWith(PersonNotFoundException(id))
            .flatMap { person -> outboxService.emitEvent(EventType.DELETED, person).map { person } }
            .onItem()
            .ignore()
            .andContinueWithNull()
}
