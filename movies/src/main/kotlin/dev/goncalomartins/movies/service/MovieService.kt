package dev.goncalomartins.movies.service

import dev.goncalomartins.movies.exception.MovieNotFoundException
import dev.goncalomartins.movies.model.movie.Movie
import dev.goncalomartins.movies.model.movie.Movies
import dev.goncalomartins.movies.model.outbox.EventType
import dev.goncalomartins.movies.repository.MovieRepository
import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.opentracing.Traced
import javax.enterprise.context.ApplicationScoped
import javax.transaction.Transactional

@ApplicationScoped
@Traced
class MovieService(
    val repository: MovieRepository,
    val outboxService: OutboxService
) {

    companion object {
        const val DEFAULT_LIMIT = 20
        const val DEFAULT_SKIP = 0
    }

    @Transactional
    fun create(movie: Movie): Uni<Movie> = repository.persist(movie)
        .flatMap { newMovie -> outboxService.emitEvent(EventType.CREATED, newMovie).map { newMovie } }

    @Transactional
    fun update(movie: Movie): Uni<Movie> = repository
        .update(movie)
        .onItem()
        .ifNull()
        .failWith(MovieNotFoundException(movie.id!!.toHexString()))
        .flatMap { updatedMovie -> outboxService.emitEvent(EventType.UPDATED, updatedMovie).map { updatedMovie } }

    fun findAll(limit: Int = DEFAULT_LIMIT, skip: Int = DEFAULT_SKIP): Uni<Movies> = repository.count()
        .flatMap { total ->
            repository.findAll(limit = limit, skip = skip).collect().asList()
                .map { movies -> Movies(total = total, movies = movies) }
        }

    fun findOne(id: String): Uni<Movie> =
        repository
            .findOne(id)
            .onItem()
            .ifNull()
            .failWith(MovieNotFoundException(id))
            .map { it!! }

    @Transactional
    fun delete(id: String): Uni<Void> =
        repository
            .delete(id)
            .onItem()
            .ifNull()
            .failWith(MovieNotFoundException(id))
            .flatMap { movie -> outboxService.emitEvent(EventType.DELETED, movie).map { movie } }
            .onItem()
            .ignore()
            .andContinueWithNull()
}
