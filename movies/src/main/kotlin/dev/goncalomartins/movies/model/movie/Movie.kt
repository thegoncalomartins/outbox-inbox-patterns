package dev.goncalomartins.movies.model.movie

import dev.goncalomartins.movies.model.outbox.AggregateType
import dev.goncalomartins.movies.model.outbox.IPayload
import io.quarkus.mongodb.panache.common.MongoEntity
import io.vertx.core.json.JsonObject
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId
import java.time.Instant
import java.time.ZoneId

@MongoEntity(collection = "movies")
class Movie() : IPayload {
    var id: ObjectId? = null

    lateinit var name: String

    var year: Int = Instant.now().atZone(ZoneId.systemDefault()).year

    @BsonProperty("directed_by")
    var directedBy: Set<String> = emptySet()

    var cast: Set<String> = emptySet()

    @BsonProperty("created_at")
    var createdAt: Instant = Instant.now()

    @BsonProperty("updated_at")
    var updatedAt: Instant = Instant.now()

    constructor(
        id: ObjectId? = null,
        name: String,
        year: Int,
        directedBy: Set<String>,
        cast: Set<String>
    ) : this() {
        this.id = id
        this.name = name
        this.year = year
        this.directedBy = directedBy
        this.cast = cast
    }

    override fun aggregateId(): String = id!!.toHexString()

    override fun aggregateType(): AggregateType = AggregateType.MOVIE

    override fun toJson(): JsonObject =
        JsonObject()
            .put("id", id!!.toHexString())
            .put("name", name)
            .put("year", year)
            .put("directed_by", directedBy)
            .put("cast", cast)
            .put("created_at", createdAt)
            .put("updated_at", updatedAt)
}
