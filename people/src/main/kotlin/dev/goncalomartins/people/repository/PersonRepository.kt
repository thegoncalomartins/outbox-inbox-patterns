package dev.goncalomartins.people.repository

import com.mongodb.client.model.Aggregates.limit
import com.mongodb.client.model.Aggregates.skip
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates.combine
import com.mongodb.client.model.Updates.set
import dev.goncalomartins.people.model.person.Person
import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheMongoRepository
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import org.bson.types.ObjectId
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class PersonRepository : ReactivePanacheMongoRepository<Person> {

    override fun update(entity: Person): Uni<Person> = mongoCollection()
        .findOneAndUpdate(
            eq("_id", entity.id),
            combine(
                set("name", entity.name),
                set("birth_date", entity.birthDate),
                set("updated_at", entity.updatedAt)
            ),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )

    fun findAll(limit: Int, skip: Int): Multi<Person> =
        mongoCollection()
            .aggregate(listOf(skip(skip), limit(limit)))

    fun findOne(id: String): Uni<Person?> = findById(ObjectId(id))

    fun delete(id: String): Uni<Person> = mongoCollection()
        .findOneAndDelete(eq("_id", ObjectId(id)))
}
