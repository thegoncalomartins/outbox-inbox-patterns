package dev.goncalomartins.people.model.person

import dev.goncalomartins.people.model.outbox.AggregateType
import dev.goncalomartins.people.model.outbox.IPayload
import io.quarkus.mongodb.panache.common.MongoEntity
import io.vertx.core.json.JsonObject
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId
import java.time.Instant
import java.time.LocalDate

@MongoEntity(collection = "people")
class Person() : IPayload {
    var id: ObjectId? = null

    lateinit var name: String

    @BsonProperty("birth_date")
    var birthDate: LocalDate = LocalDate.now()

    @BsonProperty("created_at")
    var createdAt: Instant = Instant.now()

    @BsonProperty("updated_at")
    var updatedAt: Instant = Instant.now()

    constructor(
        id: ObjectId? = null,
        name: String,
        birthDate: LocalDate
    ) : this() {
        this.id = id
        this.name = name
        this.birthDate = birthDate
    }

    override fun aggregateId(): String = id!!.toHexString()

    override fun aggregateType(): AggregateType = AggregateType.PERSON

    override fun toJson(): JsonObject =
        JsonObject()
            .put("id", id!!.toHexString())
            .put("name", name)
            .put("birth_date", birthDate)
            .put("created_at", createdAt)
            .put("updated_at", updatedAt)
}
