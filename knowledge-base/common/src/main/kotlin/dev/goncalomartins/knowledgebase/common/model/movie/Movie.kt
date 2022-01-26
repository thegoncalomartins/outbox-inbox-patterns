package dev.goncalomartins.knowledgebase.common.model.movie

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dev.goncalomartins.knowledge-base.common.model.graph.NodeMetadata
import dev.goncalomartins.knowledge-base.common.model.inbox.InboxEvent
import io.vertx.core.json.JsonObject
import org.neo4j.driver.types.Node
import java.time.Instant
import java.time.ZoneOffset

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Movie(
    val id: String,
    val title: String,
    val released: Int,
    val directedBy: Set<String>? = emptySet(),
    val cast: Set<Actor>? = emptySet(),
    val createdAt: Instant?,
    val updatedAt: Instant?
) : NodeMetadata {
    companion object {
        fun fromInboxEvent(inboxEvent: InboxEvent): Movie = JsonObject(inboxEvent.payload).mapTo(Movie::class.java)

        fun fromNode(node: Node) =
            Movie(
                id = node.get("id").asString(),
                title = node.get("title").asString(),
                released = node.get("released").asInt(),
                createdAt = node.get("created_at").asLocalDateTime().toInstant(ZoneOffset.UTC),
                updatedAt = node.get("updated_at").asLocalDateTime().toInstant(ZoneOffset.UTC)
            )
    }
}
