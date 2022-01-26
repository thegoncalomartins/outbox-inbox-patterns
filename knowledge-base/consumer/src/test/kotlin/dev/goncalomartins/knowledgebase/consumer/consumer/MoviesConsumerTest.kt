package dev.goncalomartins.knowledgebase.consumer.consumer

import dev.goncalomartins.knowledge-base.common.model.movie.Movie
import dev.goncalomartins.knowledge-base.common.service.MovieService
import dev.goncalomartins.knowledge-base.common.service.PersonService
import dev.goncalomartins.knowledgebase.consumer.util.TestUtils
import io.quarkus.test.junit.QuarkusTest
import io.smallrye.reactive.messaging.kafka.KafkaRecord
import io.vertx.core.json.JsonObject
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.assertAll
import java.time.Duration
import java.time.Instant
import java.util.UUID
import javax.inject.Inject

@QuarkusTest
@TestMethodOrder(MethodOrderer.DisplayName::class)
class MoviesConsumerTest {
    private companion object {
        const val CREATE_MOVIE_FILENAME = "createMovie.json"
        const val UPDATE_MOVIE_FILENAME = "updateMovie.json"
        const val DELETE_MOVIE_FILENAME = "deleteMovie.json"
        val DURATION: Duration = Duration.ofSeconds(3)
    }

    @Inject
    lateinit var movieService: MovieService

    @Inject
    lateinit var personService: PersonService

    @Inject
    lateinit var testUtils: TestUtils

    private val personId = UUID.randomUUID().toString()

    private val movieId = UUID.randomUUID().toString()

    @Test
    @DisplayName("1 - Test Movie Creation")
    fun testMovieCreation() {
        val createPersonOutboxEventId = UUID.randomUUID().toString()
        val createPersonPayload = JsonObject(
            String.format(
                testUtils.readFileAsString(PeopleConsumerTest.CREATE_PERSON_FILENAME),
                createPersonOutboxEventId,
                personId,
                personId
            )
        )
        val personPayload = JsonObject(createPersonPayload.getString("payload"))
        val createPersonRecord = KafkaRecord.of(createPersonPayload.getString("id"), createPersonPayload.encode())

        val createMovieOutboxEventId = UUID.randomUUID().toString()
        val createMoviePayload = JsonObject(
            String.format(
                testUtils.readFileAsString(CREATE_MOVIE_FILENAME),
                createMovieOutboxEventId,
                movieId,
                movieId,
                personId,
                personId
            )
        )
        val moviePayload = JsonObject(createMoviePayload.getString("payload"))
        val createMovieRecord = KafkaRecord.of(createMoviePayload.getString("id"), createMoviePayload.encode())

        testUtils.producePeopleMessage(createPersonRecord)

        await
            .atMost(Duration.ofMinutes(1)) // first test needs more time due to application launch
            .until { personService.findOne(personPayload.getString("id")).await().indefinitely().nodes().size == 1 }

        testUtils.produceMoviesMessage(createMovieRecord)

        await
            .atMost(DURATION)
            .untilAsserted {
                val graph = movieService.findOne(moviePayload.getString("id"))
                    .await()
                    .indefinitely()

                assertAll(
                    { assertEquals(2, graph.total()) },
                    { assertEquals(2, graph.nodes().size) },
                    { assertEquals(2, graph.edges().size) },
                    {
                        graph.nodes().forEach { node ->
                            if (node.metadata is Movie) {

                                val movie = node.metadata as Movie

                                assertAll(
                                    { assertEquals(moviePayload.getString("id"), movie.id) },
                                    { assertEquals(moviePayload.getString("title"), movie.title) },
                                    { assertEquals(moviePayload.getInteger("released"), movie.released) },
                                    {
                                        assertEquals(
                                            Instant.parse(moviePayload.getString("created_at")),
                                            movie.createdAt
                                        )
                                    },
                                    {
                                        assertEquals(
                                            Instant.parse(moviePayload.getString("updated_at")),
                                            movie.updatedAt
                                        )
                                    },
                                )
                            }
                        }
                    },
                    {
                        val personNodeId = graph.nodes().first { node -> node.label == "Person" }.id
                        val movieNodeId = graph.nodes().first { node -> node.label == "Movie" }.id
                        val role = moviePayload.getJsonArray("cast").getJsonObject(0).getString("role")

                        assertAll(
                            {
                                assertTrue(
                                    graph.edges()
                                        .any { edge -> edge.from == personNodeId && edge.to == movieNodeId && edge.relationship == "ACTED_IN" && edge.metadata["role"] == role }
                                )
                            },
                            {
                                assertTrue(
                                    graph.edges()
                                        .any { edge -> edge.from == movieNodeId && edge.to == personNodeId && edge.relationship == "DIRECTED_BY" }
                                )
                            }
                        )
                    }
                )
            }
    }

    @Test
    @DisplayName("2 - Test Movie Update")
    fun testMovieUpdate() {
        val outboxEventId = UUID.randomUUID().toString()
        val payload = JsonObject(
            String.format(
                testUtils.readFileAsString(UPDATE_MOVIE_FILENAME),
                outboxEventId,
                movieId,
                movieId,
                personId,
                personId
            )
        )
        val moviePayload = JsonObject(payload.getString("payload"))
        val record = KafkaRecord.of(payload.getString("id"), payload.encode())

        testUtils.produceMoviesMessage(record)

        await
            .atMost(DURATION)
            .untilAsserted {
                val graph = movieService.findOne(moviePayload.getString("id"))
                    .await()
                    .indefinitely()

                assertAll(
                    { assertEquals(2, graph.total()) },
                    { assertEquals(2, graph.nodes().size) },
                    { assertEquals(2, graph.edges().size) },
                    {
                        graph.nodes().forEach { node ->
                            if (node.metadata is Movie) {

                                val movie = node.metadata as Movie

                                assertAll(
                                    { assertEquals(moviePayload.getString("id"), movie.id) },
                                    { assertEquals(moviePayload.getString("title"), movie.title) },
                                    { assertEquals(moviePayload.getInteger("released"), movie.released) },
                                    {
                                        assertEquals(
                                            Instant.parse(moviePayload.getString("created_at")),
                                            movie.createdAt
                                        )
                                    },
                                    {
                                        assertEquals(
                                            Instant.parse(moviePayload.getString("updated_at")),
                                            movie.updatedAt
                                        )
                                    },
                                )
                            }
                        }
                    }
                )
            }
    }

    @Test
    @DisplayName("3 - Test Movie Deletion")
    fun testMovieDeletion() {
        val outboxEventId = UUID.randomUUID().toString()
        val payload = JsonObject(
            String.format(
                testUtils.readFileAsString(DELETE_MOVIE_FILENAME),
                outboxEventId,
                movieId,
                movieId,
                personId,
                personId
            )
        )
        val moviePayload = JsonObject(payload.getString("payload"))
        val record = KafkaRecord.of(payload.getString("id"), payload.encode())

        testUtils.produceMoviesMessage(record)

        await
            .atMost(DURATION)
            .untilAsserted {
                val graph = movieService.findOne(moviePayload.getString("id"))
                    .await()
                    .indefinitely()

                assertAll(
                    { assertEquals(0, graph.total()) },
                    { assertTrue(graph.isEmpty()) },
                )
            }
    }

    @Test
    @DisplayName("4 - Test Event Already Consumed")
    fun testEventAlreadyConsumed() {
        testMovieDeletion()
    }
}
