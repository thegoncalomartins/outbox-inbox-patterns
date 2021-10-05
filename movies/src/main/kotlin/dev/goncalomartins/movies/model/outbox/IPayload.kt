package dev.goncalomartins.movies.model.outbox

import io.vertx.core.json.JsonObject

interface IPayload {

    fun aggregateId(): String

    fun aggregateType(): AggregateType

    fun toJson(): JsonObject
}
