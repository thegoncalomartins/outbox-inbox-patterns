package dev.goncalomartins.movies.service

import dev.goncalomartins.movies.model.outbox.EventType
import dev.goncalomartins.movies.model.outbox.IPayload
import dev.goncalomartins.movies.model.outbox.OutboxEvent
import dev.goncalomartins.movies.repository.OutboxRepository
import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.opentracing.Traced
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
@Traced
class OutboxService(
    val repository: OutboxRepository
) {
    fun emitEvent(eventType: EventType, payload: IPayload): Uni<Void> = repository.persist(
        OutboxEvent(
            aggregateId = payload.aggregateId(),
            aggregateType = payload.aggregateType(),
            eventType = eventType,
            payload = payload.toJson().encode()
        )
    ).flatMap { repository.delete(it) }
}
