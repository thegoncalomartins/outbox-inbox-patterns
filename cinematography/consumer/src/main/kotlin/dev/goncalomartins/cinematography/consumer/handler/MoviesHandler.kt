package dev.goncalomartins.cinematography.consumer.handler

import dev.goncalomartins.cinematography.common.model.inbox.EventType
import dev.goncalomartins.cinematography.common.model.inbox.InboxEvent
import dev.goncalomartins.cinematography.common.model.movie.Movie
import dev.goncalomartins.cinematography.common.service.InboxService
import dev.goncalomartins.cinematography.common.service.MovieService
import dev.goncalomartins.cinematography.common.util.DatabaseUtils
import dev.goncalomartins.cinematography.consumer.exception.EventAlreadyConsumedException
import io.smallrye.mutiny.Uni
import org.neo4j.driver.reactive.RxTransaction
import java.util.function.BiFunction
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class MoviesHandler(
    val databaseUtils: DatabaseUtils,
    val inboxService: InboxService,
    val movieService: MovieService
) : Handler {

    private val events = mapOf(
        EventType.CREATED to BiFunction<RxTransaction, Movie, Uni<Void>> { transaction, movie ->
            movieService.save(
                transaction,
                movie
            )
        },
        EventType.UPDATED to BiFunction<RxTransaction, Movie, Uni<Void>> { transaction, movie ->
            movieService.save(
                transaction,
                movie
            )
        },
        EventType.DELETED to BiFunction<RxTransaction, Movie, Uni<Void>> { transaction, movie ->
            movieService.delete(
                transaction,
                movie
            )
        }
    )

    override fun handle(inboxEvent: InboxEvent): Uni<Void> =
        databaseUtils.inTransaction { transaction ->
            eventNotAlreadyConsumedAndSave(transaction, inboxEvent)
                .flatMap {
                    Uni.createFrom().item { Movie.fromInboxEvent(inboxEvent) }
                }
                .flatMap { movie ->
                    events[inboxEvent.eventType]!!.apply(transaction, movie)
                }
        }

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
