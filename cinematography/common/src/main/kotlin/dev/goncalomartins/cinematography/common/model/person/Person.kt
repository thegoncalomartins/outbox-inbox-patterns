package dev.goncalomartins.cinematography.common.model.person

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dev.goncalomartins.cinematography.common.model.inbox.InboxEvent
import io.vertx.core.json.JsonObject
import org.neo4j.driver.types.Node
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Person(
    val id: String,
    val name: String,
    val birthDate: LocalDate?,
    val createdAt: Instant?,
    val updatedAt: Instant?
) {
    companion object {
        fun fromInboxEvent(inboxEvent: InboxEvent): Person = JsonObject(inboxEvent.payload).mapTo(Person::class.java)

        fun fromNode(node: Node) =
            Person(
                node.get("id").asString(),
                node.get("name").asString(),
                node.get("birth_date").asLocalDate(),
                node.get("created_at").asLocalDateTime().toInstant(ZoneOffset.UTC),
                node.get("updated_at").asLocalDateTime().toInstant(ZoneOffset.UTC)
            )
    }
}
