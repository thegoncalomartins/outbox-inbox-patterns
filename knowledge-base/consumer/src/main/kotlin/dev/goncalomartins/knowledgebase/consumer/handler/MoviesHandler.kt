package dev.goncalomartins.knowledgebase.consumer.handler

import dev.goncalomartins.knowledgebase.common.model.inbox.EventType
import dev.goncalomartins.knowledgebase.common.model.inbox.InboxEvent
import dev.goncalomartins.knowledgebase.common.model.movie.Movie
import dev.goncalomartins.knowledgebase.common.service.InboxService
import dev.goncalomartins.knowledgebase.common.service.MovieService
import dev.goncalomartins.knowledgebase.common.util.DatabaseUtils
import dev.goncalomartins.knowledgebase.consumer.exception.EventAlreadyConsumedException
import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.opentracing.Traced
import org.neo4j.driver.reactive.RxTransaction
import java.util.function.BiFunction
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
@Traced
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
