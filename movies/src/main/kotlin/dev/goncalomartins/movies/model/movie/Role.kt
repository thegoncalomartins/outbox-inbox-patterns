package dev.goncalomartins.movies.model.movie

import io.vertx.core.json.JsonObject
import org.bson.codecs.pojo.annotations.BsonProperty

class Role() {
    @BsonProperty("person_id")
    lateinit var personId: String

    lateinit var role: String

    constructor(personId: String, role: String) : this() {
        this.personId = personId
        this.role = role
    }

    fun toJson(): JsonObject = JsonObject()
        .put("person_id", personId)
        .put("role", role)
}
