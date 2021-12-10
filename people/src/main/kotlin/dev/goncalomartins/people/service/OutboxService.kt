package dev.goncalomartins.people.service

import dev.goncalomartins.people.model.outbox.EventType
import dev.goncalomartins.people.model.outbox.IPayload
import dev.goncalomartins.people.model.outbox.OutboxEvent
import dev.goncalomartins.people.repository.OutboxRepository
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
