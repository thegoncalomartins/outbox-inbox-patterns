package dev.goncalomartins.cinematography.common.model.inbox

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.vertx.core.json.JsonObject
import org.neo4j.driver.types.Node
import java.time.Instant
import java.time.ZoneOffset

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
class InboxEvent() {

    lateinit var id: String

    lateinit var aggregateId: String

    lateinit var eventType: EventType

    lateinit var aggregateType: AggregateType

    lateinit var payload: String

    lateinit var timestamp: Instant

    companion object {
        fun fromNode(node: Node) = InboxEvent()
            .apply {
                id = node.get("id").asString()
                aggregateId = node.get("aggregate_id").asString()
                eventType = EventType.valueOf(node.get("event_type").asString())
                aggregateType = AggregateType.valueOf(node.get("aggregate_type").asString())
                payload = node.get("payload").asString()
                timestamp = node.get("timestamp").asLocalDateTime().toInstant(ZoneOffset.UTC)
            }

        fun fromJsonObject(json: JsonObject) = InboxEvent()
            .apply {
                id = json.getString("id")
                aggregateId = json.getString("aggregate_id")
                eventType = EventType.valueOf(json.getString("event_type"))
                aggregateType = AggregateType.valueOf(json.getString("aggregate_type"))
                payload = json.getString("payload")
                timestamp = Instant.ofEpochMilli(json.getLong("timestamp"))
            }
    }
}
