package dev.goncalomartins.knowledgebase.common.service

import dev.goncalomartins.knowledgebase.common.model.graph.Graph
import dev.goncalomartins.knowledgebase.common.model.movie.Movie
import dev.goncalomartins.knowledgebase.common.model.movie.Movies
import dev.goncalomartins.knowledgebase.common.repository.MovieRepository
import dev.goncalomartins.knowledgebase.common.util.DatabaseUtils
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.neo4j.driver.reactive.RxTransaction

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MovieServiceTest {

    private lateinit var movieRepository: MovieRepository

    private lateinit var databaseUtils: DatabaseUtils

    private lateinit var subject: MovieService

    @BeforeEach
    fun setup() {
        movieRepository = mock(MovieRepository::class.java)
        databaseUtils = mock(DatabaseUtils::class.java)
        subject = MovieService(movieRepository, databaseUtils)
    }

    @Test
    fun testFindOne() {
        val transaction = mock(RxTransaction::class.java)

        val graph = Graph()
        val id = "id"

        whenever(databaseUtils.inTransaction(any() as (RxTransaction) -> Uni<Graph>))
            .then { it.getArgument<(RxTransaction) -> Uni<Graph>>(0).invoke(transaction) }

        whenever(
            movieRepository.findOne(
                transaction,
                id,
                PersonService.DEFAULT_SKIP,
                PersonService.DEFAULT_LIMIT
            )
        ).thenReturn(Uni.createFrom().item(graph))

        assertEquals(graph, subject.findOne(id).await().indefinitely())

        verify(movieRepository, times(1))
            .findOne(
                transaction,
                id,
                PersonService.DEFAULT_SKIP,
                PersonService.DEFAULT_LIMIT
            )
    }

    @Test
    fun testFindAll() {
        val transaction = mock(RxTransaction::class.java)

        val movies = Movies(0, emptyList())

        whenever(databaseUtils.inTransaction(any() as (RxTransaction) -> Uni<Graph>))
            .then { it.getArgument<(RxTransaction) -> Uni<Graph>>(0).invoke(transaction) }

        whenever(
            movieRepository.count(
                transaction
            )
        ).thenReturn(Uni.createFrom().item(0L))

        whenever(
            movieRepository.findAll(
                transaction,
                PersonService.DEFAULT_SKIP,
                PersonService.DEFAULT_LIMIT
            )
        ).thenReturn(Multi.createFrom().empty())

        assertEquals(movies, subject.findAll().await().indefinitely())

        verify(movieRepository, times(1))
            .count(transaction)

        verify(movieRepository, times(1))
            .findAll(transaction, MovieService.DEFAULT_SKIP, MovieService.DEFAULT_LIMIT)
    }

    @Test
    fun testSave() {
        val transaction = mock(RxTransaction::class.java)

        val movie = Movie("id", "title", 2022, emptySet(), emptySet(), null, null)

        val uni = Uni.createFrom().voidItem()

        whenever(movieRepository.save(transaction, movie))
            .thenReturn(uni)

        subject.save(transaction, movie).await().indefinitely()

        verify(movieRepository, times(1)).save(transaction, movie)
    }

    @Test
    fun testDelete() {
        val transaction = mock(RxTransaction::class.java)

        val movie = Movie("id", "title", 2022, emptySet(), emptySet(), null, null)

        val uni = Uni.createFrom().voidItem()

        whenever(movieRepository.delete(transaction, movie))
            .thenReturn(uni)

        subject.delete(transaction, movie).await().indefinitely()

        verify(movieRepository, times(1)).delete(transaction, movie)
    }
}
