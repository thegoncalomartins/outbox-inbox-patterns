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
    var repository: PersonRepository,
    var outboxService: OutboxService
) {

    companion object {
        const val DEFAULT_LIMIT = 20
        const val DEFAULT_SKIP = 0
    }

    @Transactional
    fun create(movie: Person): Uni<Person> = repository.persist(movie)
        .flatMap { newMovie -> outboxService.emitEvent(EventType.CREATED, newMovie).map { newMovie } }

    @Transactional
    fun update(movie: Person): Uni<Person> = repository
        .update(movie)
        .onItem()
        .ifNull()
        .failWith(PersonNotFoundException(movie.id!!.toHexString()))
        .flatMap { updatedMovie -> outboxService.emitEvent(EventType.UPDATED, updatedMovie).map { updatedMovie } }

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
            .flatMap { movie -> outboxService.emitEvent(EventType.DELETED, movie).map { movie } }
            .onItem()
            .ignore()
            .andContinueWithNull()
}
