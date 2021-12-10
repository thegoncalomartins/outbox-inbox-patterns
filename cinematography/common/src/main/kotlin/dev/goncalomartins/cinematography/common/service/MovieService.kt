package dev.goncalomartins.cinematography.common.service

import dev.goncalomartins.cinematography.common.model.movie.Movie
import dev.goncalomartins.cinematography.common.repository.MovieRepository
import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.opentracing.Traced
import org.neo4j.driver.reactive.RxTransaction
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
@Traced
class MovieService(val movieRepository: MovieRepository) {

    fun save(transaction: RxTransaction, movie: Movie): Uni<Void> = movieRepository.save(transaction, movie)

    fun delete(transaction: RxTransaction, movie: Movie): Uni<Void> = movieRepository.delete(transaction, movie)
}
