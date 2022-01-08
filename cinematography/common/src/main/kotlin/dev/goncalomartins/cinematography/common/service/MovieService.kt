package dev.goncalomartins.cinematography.common.service

import dev.goncalomartins.cinematography.common.model.graph.Graph
import dev.goncalomartins.cinematography.common.model.movie.Movie
import dev.goncalomartins.cinematography.common.model.movie.Movies
import dev.goncalomartins.cinematography.common.repository.MovieRepository
import dev.goncalomartins.cinematography.common.util.DatabaseUtils
import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.opentracing.Traced
import org.neo4j.driver.reactive.RxTransaction
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
@Traced
class MovieService(val movieRepository: MovieRepository, val databaseUtils: DatabaseUtils) {

    companion object {
        const val DEFAULT_LIMIT = 20
        const val DEFAULT_SKIP = 0
    }

    fun findOne(id: String, skip: Int, limit: Int): Uni<Graph> =
        databaseUtils.inTransaction { transaction ->
            movieRepository.findOne(transaction, id, skip, limit)
        }

    fun findAll(skip: Int, limit: Int): Uni<Movies> =
        databaseUtils.inTransaction { transaction ->
            movieRepository.count(transaction)
                .flatMap { total ->
                    movieRepository.findAll(transaction, skip, limit)
                        .collect()
                        .asList()
                        .map {
                            Movies(total, it)
                        }
                }
        }

    fun save(transaction: RxTransaction, movie: Movie): Uni<Void> = movieRepository.save(transaction, movie)

    fun delete(transaction: RxTransaction, movie: Movie): Uni<Void> = movieRepository.delete(transaction, movie)
}
