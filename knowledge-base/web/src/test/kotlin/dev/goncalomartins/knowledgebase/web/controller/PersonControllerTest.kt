package dev.goncalomartins.knowledgebase.web.controller

import dev.goncalomartins.knowledgebase.common.model.movie.Actor
import dev.goncalomartins.knowledgebase.common.model.movie.Movie
import dev.goncalomartins.knowledgebase.common.model.person.Person
import dev.goncalomartins.knowledgebase.common.service.MovieService
import dev.goncalomartins.knowledgebase.common.service.PersonService
import dev.goncalomartins.knowledgebase.common.util.DatabaseUtils
import dev.goncalomartins.knowledgebase.web.dto.graph.GraphDto
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.Header
import io.restassured.http.Headers
import io.vertx.core.json.JsonObject
import org.apache.http.HttpStatus
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.assertAll
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType

@QuarkusTest
@TestMethodOrder(MethodOrderer.DisplayName::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PersonControllerTest {

    @Inject
    lateinit var personService: PersonService

    @Inject
    lateinit var movieService: MovieService

    @Inject
    lateinit var databaseUtils: DatabaseUtils

    val person = Person(UUID.randomUUID().toString(), "name", LocalDate.now(), Instant.now(), Instant.now())
    val movie = Movie(
        UUID.randomUUID().toString(),
        "title",
        2022,
        setOf(person.id),
        setOf(Actor(person.id, "role")),
        Instant.now(),
        Instant.now()
    )

    @BeforeAll
    fun setup() {
        createPerson()
        createMovie()
    }

    @Test
    @DisplayName("1 - List People")
    fun testListPeople() {
        val responseBody = JsonObject(
            RestAssured
                .given()
                .headers(
                    Headers.headers(
                        Header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                    )
                )
                .`when`()
                .get(PersonController.PATH)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .body()
                .asString()
        )

        assertAll(
            { assertNotNull(responseBody.getInteger("total")) },
            { assertEquals(1, responseBody.getInteger("total")) },
            { assertNotNull(responseBody.getJsonObject("_embedded")) },
            { assertNotNull(responseBody.getJsonObject("_embedded").getJsonArray("people")) },
            { assertNotNull(responseBody.getJsonObject("_embedded").getJsonArray("people").getJsonObject(0)) },
            {
                assertNotNull(
                    responseBody.getJsonObject("_embedded").getJsonArray("people").getJsonObject(0).getString("id")
                )
            },
            {
                assertEquals(
                    person.name,
                    responseBody.getJsonObject("_embedded").getJsonArray("people").getJsonObject(0).getString("name")
                )
            },
            {
                assertEquals(
                    person.birthDate.toString(),
                    responseBody.getJsonObject("_embedded").getJsonArray("people").getJsonObject(0)
                        .getString("birth_date")
                )
            },
            {
                assertNotNull(
                    responseBody.getJsonObject("_embedded").getJsonArray("people").getJsonObject(0)
                        .getString("created_at")
                )
            },
            {
                assertNotNull(
                    responseBody.getJsonObject("_embedded").getJsonArray("people").getJsonObject(0)
                        .getString("updated_at")
                )
            },
            { assertNotNull(responseBody.getJsonObject("_links")) },
            { assertNotNull(responseBody.getJsonObject("_links").getJsonObject("self")) },
            { assertNotNull(responseBody.getJsonObject("_links").getJsonObject("first")) },
            { assertNotNull(responseBody.getJsonObject("_links").getJsonObject("previous")) },
            { assertNotNull(responseBody.getJsonObject("_links").getJsonObject("next")) },
            { assertNotNull(responseBody.getJsonObject("_links").getJsonObject("last")) },
            { assertNotNull(responseBody.getJsonObject("_links").getJsonObject("self").getString("href")) },
            { assertNotNull(responseBody.getJsonObject("_links").getJsonObject("first").getString("href")) },
            { assertNotNull(responseBody.getJsonObject("_links").getJsonObject("previous").getString("href")) },
            { assertNotNull(responseBody.getJsonObject("_links").getJsonObject("next").getString("href")) },
            { assertNotNull(responseBody.getJsonObject("_links").getJsonObject("last").getString("href")) },
            {
                assertTrue(
                    responseBody.getJsonObject("_embedded").getJsonArray("people").getJsonObject(0)
                        .getJsonObject("_links").getJsonObject("self").getString("href")
                        .contains("${PersonController.PATH}/${person.id}")
                )
            },
        )
    }

    @Test
    @DisplayName("2 - Read Nonexistent Person")
    fun testReadNonexistentPerson() {

        val responseBody = JsonObject(
            RestAssured
                .given()
                .headers(
                    Headers.headers(
                        Header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                    )
                )
                .`when`()
                .get("${PersonController.PATH}/61a66a521ec5f616a4fc2f9f")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .extract()
                .body()
                .asString()
        )

        assertAll(
            { assertEquals("404", responseBody.getString("code")) },
            {
                assertEquals(
                    "Person with id '61a66a521ec5f616a4fc2f9f' does not exist",
                    responseBody.getString("message")
                )
            },
        )
    }

    @Test
    @DisplayName("3 - Read Person")
    fun testReadPerson() {
        val responseBody = JsonObject(
            RestAssured
                .given()
                .headers(
                    Headers.headers(
                        Header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                    )
                )
                .`when`()
                .get("${PersonController.PATH}/${person.id}")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .body()
                .asString()
        )

        val graph = responseBody.getJsonObject("_embedded").getJsonObject("graph").mapTo(GraphDto::class.java)

        assertAll(
            { assertEquals(2, graph.nodes.size) },
            { assertEquals(2, graph.edges.size) },
            {
                graph.nodes.forEach { node ->
                    if (node.metadata is Movie) {

                        val movie = node.metadata as Movie

                        Assertions.assertAll(
                            { assertEquals(this.movie.id, movie.id) },
                            { assertEquals(this.movie.title, movie.title) },
                            { assertEquals(this.movie.released, movie.released) },
                            {
                                assertEquals(
                                    this.movie.createdAt,
                                    movie.createdAt
                                )
                            },
                            {
                                assertEquals(
                                    this.movie.updatedAt,
                                    movie.updatedAt
                                )
                            },
                        )
                    }

                    if (node.metadata is Person) {

                        val person = node.metadata as Person

                        Assertions.assertAll(
                            { assertEquals(this.person.id, person.id) },
                            { assertEquals(this.person.name, person.name) },
                            { assertEquals(this.person.birthDate, person.birthDate) },
                            {
                                assertEquals(
                                    this.person.createdAt,
                                    person.createdAt
                                )
                            },
                            {
                                assertEquals(
                                    this.person.updatedAt,
                                    person.updatedAt
                                )
                            },
                        )
                    }
                }
            },
            {
                val personNodeId = graph.nodes.first { node -> node.label == "Person" }.id
                val movieNodeId = graph.nodes.first { node -> node.label == "Movie" }.id
                val role = this.movie.cast?.elementAt(0)?.role

                assertAll(
                    {
                        assertTrue(
                            graph.edges
                                .any { edge ->
                                    edge.from == personNodeId && edge.to == movieNodeId && edge.relationship == "ACTED_IN" && edge.metadata?.get(
                                        "role"
                                    ) == role
                                }
                        )
                    },
                    {
                        assertTrue(
                            graph.edges
                                .any { edge -> edge.from == movieNodeId && edge.to == personNodeId && edge.relationship == "DIRECTED_BY" }
                        )
                    }
                )
            }
        )
    }

    @AfterAll
    fun teardown() {
        deletePerson()
        deleteMovie()
    }

    private fun createPerson() {
        databaseUtils.inTransaction { transaction ->
            personService.save(transaction, person)
        }.await().indefinitely()
    }

    private fun createMovie() {
        databaseUtils.inTransaction { transaction ->
            movieService.save(transaction, movie)
        }.await().indefinitely()
    }

    private fun deletePerson() {
        databaseUtils.inTransaction { transaction ->
            personService.delete(transaction, person)
        }.await().indefinitely()
    }

    private fun deleteMovie() {
        databaseUtils.inTransaction { transaction ->
            movieService.delete(transaction, movie)
        }.await().indefinitely()
    }
}
