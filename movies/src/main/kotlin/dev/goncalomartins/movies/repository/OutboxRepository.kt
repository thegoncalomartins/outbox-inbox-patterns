package dev.goncalomartins.movies.repository

import dev.goncalomartins.movies.model.outbox.OutboxEvent
import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheMongoRepository
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class OutboxRepository : ReactivePanacheMongoRepository<OutboxEvent>
