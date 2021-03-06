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

    lateinit var title: String

    var released: Int = Instant.now().atZone(ZoneId.systemDefault()).year

    @BsonProperty("directed_by")
    var directedBy: Set<String> = emptySet()

    var cast: Set<Actor> = emptySet()

    @BsonProperty("created_at")
    var createdAt: Instant = Instant.now()

    @BsonProperty("updated_at")
    var updatedAt: Instant = Instant.now()

    constructor(
        id: ObjectId? = null,
        title: String,
        released: Int,
        directedBy: Set<String>,
        cast: Set<Actor>
    ) : this() {
        this.id = id
        this.title = title
        this.released = released
        this.directedBy = directedBy
        this.cast = cast
    }

    override fun aggregateId(): String = id!!.toHexString()

    override fun aggregateType(): AggregateType = AggregateType.MOVIE

    override fun toJson(): JsonObject =
        JsonObject()
            .put("id", id!!.toHexString())
            .put("title", title)
            .put("released", released)
            .put("directed_by", directedBy)
            .put("cast", cast.map(Actor::toJson))
            .put("created_at", createdAt)
            .put("updated_at", updatedAt)
}
