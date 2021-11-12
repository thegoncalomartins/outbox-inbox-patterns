package dev.goncalomartins.people.repository

import dev.goncalomartins.people.model.outbox.OutboxEvent
import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheMongoRepository
import org.eclipse.microprofile.opentracing.Traced
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
@Traced
class OutboxRepository : ReactivePanacheMongoRepository<OutboxEvent>
