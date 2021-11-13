package dev.goncalomartins.cinematography.consumer.consumer

import dev.goncalomartins.cinematography.common.model.inbox.InboxEvent
import dev.goncalomartins.cinematography.consumer.exception.EventAlreadyConsumedException
import dev.goncalomartins.cinematography.consumer.handler.PeopleHandler
import io.smallrye.mutiny.Uni
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord
import io.vertx.core.json.JsonObject
import org.eclipse.microprofile.opentracing.Traced
import org.eclipse.microprofile.reactive.messaging.Incoming
import org.slf4j.LoggerFactory
import java.util.function.Supplier
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
@Traced
class PeopleKafkaConsumer(val peopleHandler: PeopleHandler) : KafkaConsumer<String, String> {
    private companion object {
        const val PEOPLE_CHANNEL = "people"
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    @Incoming(PEOPLE_CHANNEL)
    override fun consume(record: IncomingKafkaRecord<String, String>): Uni<Void> =
        Uni
            .createFrom()
            .item {
                logger.info("Consuming event with payload ${record.payload}")
                InboxEvent.fromJsonObject(JsonObject(record.payload))
            }.flatMap { inboxEvent ->
                peopleHandler.handle(inboxEvent)
            }
            .flatMap {
                Uni.createFrom().completionStage {
                    logger.info("Success consuming event with payload ${record.payload}")
                    record.ack()
                }
            }
            .onFailure(EventAlreadyConsumedException::class.java)
            .recoverWithUni(
                Supplier {
                    logger.info("Event with payload ${record.payload} was already consumed, acknowledging")
                    Uni.createFrom().completionStage(record::ack)
                }
            )
            .onFailure()
            .recoverWithUni { error ->
                Uni
                    .createFrom()
                    .completionStage {
                        logger.error("Error consuming event with payload ${record.payload}: ${error.message}", error)
                        record.nack(error)
                    }
            }
}
