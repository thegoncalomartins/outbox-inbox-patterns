package dev.goncalomartins.knowledgebase.common.repository

import dev.goncalomartins.knowledgebase.common.model.inbox.InboxEvent
import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.opentracing.Traced
import org.neo4j.driver.reactive.RxTransaction
import java.time.LocalDateTime
import java.time.ZoneId
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
@Traced
class InboxRepository {
    private companion object {
        const val FIND_ONE_QUERY = "MATCH (event:InboxEvent) WHERE event.id = \$id RETURN event"
        const val CREATE_QUERY =
            """
                MERGE (:InboxEvent {id: ${'$'}id,
                aggregate_id: ${'$'}aggregate_id,
                event_type: ${'$'}event_type,
                aggregate_type: ${'$'}aggregate_type,
                payload: ${'$'}payload,
                timestamp: ${'$'}timestamp})
            """
    }

    fun findOne(transaction: RxTransaction, id: String): Uni<InboxEvent?> =
        Uni
            .createFrom()
            .publisher(
                transaction.run(
                    FIND_ONE_QUERY,
                    mapOf(
                        "id" to id
                    )
                ).records()
            )
            .map { record ->
                record?.let { InboxEvent.fromNode(it.get("event").asNode()) }
            }

    fun save(transaction: RxTransaction, inboxEvent: InboxEvent): Uni<Void> =
        Uni
            .createFrom()
            .publisher(
                transaction.run(
                    CREATE_QUERY,
                    mapOf(
                        "id" to inboxEvent.id,
                        "aggregate_id" to inboxEvent.aggregateId,
                        "event_type" to inboxEvent.eventType.name,
                        "aggregate_type" to inboxEvent.aggregateType.name,
                        "payload" to inboxEvent.payload,
                        "timestamp" to LocalDateTime.ofInstant(inboxEvent.timestamp, ZoneId.systemDefault())
                    )
                ).records()
            ).onItem()
            .ignore()
            .andContinueWithNull()
}
