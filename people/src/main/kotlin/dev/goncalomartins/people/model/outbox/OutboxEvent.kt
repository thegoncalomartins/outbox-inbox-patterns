package dev.goncalomartins.people.model.outbox

import io.quarkus.mongodb.panache.common.MongoEntity
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId
import java.time.Instant

@MongoEntity(collection = "outbox")
class OutboxEvent() {
    var id: ObjectId? = null

    @BsonProperty("aggregate_id")
    lateinit var aggregateId: String

    @BsonProperty("aggregate_type")
    lateinit var aggregateType: AggregateType

    @BsonProperty("event_type")
    lateinit var eventType: EventType

    lateinit var payload: String

    var timestamp: Instant = Instant.now()

    constructor(aggregateId: String, aggregateType: AggregateType, eventType: EventType, payload: String) : this() {
        this.aggregateId = aggregateId
        this.aggregateType = aggregateType
        this.eventType = eventType
        this.payload = payload
    }
}
