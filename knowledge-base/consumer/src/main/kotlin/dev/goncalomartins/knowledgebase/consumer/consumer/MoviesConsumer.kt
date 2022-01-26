package dev.goncalomartins.knowledgebase.consumer.consumer

import dev.goncalomartins.knowledge-base.common.model.inbox.InboxEvent
import dev.goncalomartins.knowledgebase.consumer.exception.EventAlreadyConsumedException
import dev.goncalomartins.knowledgebase.consumer.handler.MoviesHandler
import io.smallrye.mutiny.Uni
import io.vertx.core.json.JsonObject
import org.eclipse.microprofile.opentracing.Traced
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.eclipse.microprofile.reactive.messaging.Message
import org.slf4j.LoggerFactory
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
@Traced
class MoviesConsumer(val moviesHandler: MoviesHandler) : Consumer<String> {
    private companion object {
        const val MOVIES_CHANNEL = "movies"
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    @Incoming(MOVIES_CHANNEL)
    override fun consume(message: Message<String>): Uni<Void> =
        Uni
            .createFrom()
            .item {
                logger.info("Consuming event with payload ${message.payload}")
                InboxEvent.fromJsonObject(JsonObject(message.payload))
            }.flatMap { inboxEvent ->
                moviesHandler.handle(inboxEvent)
            }
            .flatMap {
                Uni
                    .createFrom()
                    .completionStage {
                        logger.info("Success consuming event with payload ${message.payload}")
                        message.ack()
                    }
            }
            .onFailure(EventAlreadyConsumedException::class.java)
            .recoverWithUni(
                Uni.createFrom().completionStage {
                    logger.info("Event with payload ${message.payload} was already consumed, acknowledging")
                    message.ack()
                }
            )
            .onFailure()
            .recoverWithUni { error ->
                Uni
                    .createFrom()
                    .completionStage {
                        logger.error("Error consuming event with payload ${message.payload}: ${error.message}", error)
                        message.nack(error)
                    }
            }
}
