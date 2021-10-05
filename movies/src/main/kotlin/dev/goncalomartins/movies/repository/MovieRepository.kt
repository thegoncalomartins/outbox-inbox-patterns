package dev.goncalomartins.movies.repository

import com.mongodb.client.model.Aggregates.limit
import com.mongodb.client.model.Aggregates.skip
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import com.mongodb.client.model.Updates.combine
import com.mongodb.client.model.Updates.set
import dev.goncalomartins.movies.model.movie.Movie
import io.quarkus.mongodb.panache.kotlin.reactive.ReactivePanacheMongoRepository
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import org.bson.types.ObjectId
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class MovieRepository : ReactivePanacheMongoRepository<Movie> {

    override fun update(entity: Movie): Uni<Movie> = mongoCollection()
        .findOneAndUpdate(
            eq("_id", entity.id),
            combine(
                set("name", entity.name),
                set("year", entity.year),
                set("directed_by", entity.directedBy),
                set("cast", entity.cast),
                set("updated_at", entity.updatedAt)
            ),
            FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
        )

    fun findAll(limit: Int, skip: Int): Multi<Movie> =
        mongoCollection()
            .aggregate(listOf(skip(skip), limit(limit)))

    fun findOne(id: String): Uni<Movie?> = findById(ObjectId(id))

    fun delete(id: String): Uni<Movie> = mongoCollection()
        .findOneAndDelete(eq("_id", ObjectId(id)))
}
