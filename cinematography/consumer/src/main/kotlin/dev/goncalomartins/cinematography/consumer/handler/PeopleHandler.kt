package dev.goncalomartins.cinematography.consumer.handler

import dev.goncalomartins.cinematography.common.model.inbox.EventType
import dev.goncalomartins.cinematography.common.model.inbox.InboxEvent
import dev.goncalomartins.cinematography.common.model.person.Person
import dev.goncalomartins.cinematography.common.service.InboxService
import dev.goncalomartins.cinematography.common.service.PersonService
import dev.goncalomartins.cinematography.common.util.DatabaseUtils
import dev.goncalomartins.cinematography.consumer.exception.EventAlreadyConsumedException
import io.smallrye.mutiny.Uni
import org.neo4j.driver.reactive.RxTransaction
import org.slf4j.LoggerFactory
import java.util.function.BiFunction
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class PeopleHandler(
    val databaseUtils: DatabaseUtils,
    val inboxService: InboxService,
    val personService: PersonService
) : Handler {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val events = mapOf(
        EventType.CREATED to BiFunction<RxTransaction, Person, Uni<Void>> { transaction, person ->
            personService.save(
                transaction,
                person
            )
        },
        EventType.UPDATED to BiFunction<RxTransaction, Person, Uni<Void>> { transaction, person ->
            personService.save(
                transaction,
                person
            )
        },
        EventType.DELETED to BiFunction<RxTransaction, Person, Uni<Void>> { transaction, person ->
            personService.delete(
                transaction,
                person
            )
        }
    )

    override fun handle(inboxEvent: InboxEvent): Uni<Void> =
        databaseUtils.inTransaction { transaction ->
            eventNotAlreadyConsumedAndSave(transaction, inboxEvent)
                .flatMap {
                    Uni.createFrom().item { Person.fromInboxEvent(inboxEvent) }
                }
                .flatMap { person ->
                    events[inboxEvent.eventType]!!.apply(transaction, person)
                }
        }.onFailure()
            .invoke { error -> logger.error(error.message, error) }

    private fun eventNotAlreadyConsumedAndSave(transaction: RxTransaction, inboxEvent: InboxEvent) =
        inboxService.findOne(transaction, inboxEvent.id)
            .onItem()
            .ifNotNull()
            .failWith {
                EventAlreadyConsumedException(inboxEvent.id)
            }
            .flatMap {
                inboxService.save(transaction, inboxEvent)
            }
}
