package dev.goncalomartins.people.repository

import dev.goncalomartins.people.model.outbox.OutboxEvent
import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheMongoRepository
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class OutboxRepository : ReactivePanacheMongoRepository<OutboxEvent>
