package dev.goncalomartins.knowledgebase.web.controller

import dev.goncalomartins.knowledgebase.common.model.movie.Actor
import dev.goncalomartins.knowledgebase.common.model.movie.Movie
import dev.goncalomartins.knowledgebase.common.model.person.Person
import dev.goncalomartins.knowledgebase.common.service.MovieService
import dev.goncalomartins.knowledgebase.common.service.PersonService
import dev.goncalomartins.knowledgebase.common.util.DatabaseUtils
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.Header
import io.restassured.http.Headers
import io.vertx.core.json.JsonObject
import org.apache.http.HttpStatus
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType

@QuarkusTest
@TestMethodOrder(MethodOrderer.DisplayName::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MovieControllerTest {

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
    @DisplayName("1 - List Movies")
    fun testListMovies() {

        val responseBody = JsonObject(
            RestAssured
                .given()
                .headers(
                    Headers.headers(
                        Header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                    )
                )
                .`when`()
                .get(MovieController.PATH)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .body()
                .asString()
        )

        assertAll(
            { Assertions.assertNotNull(responseBody.getInteger("total")) },
            { Assertions.assertEquals(1, responseBody.getInteger("total")) },
            { Assertions.assertNotNull(responseBody.getJsonObject("_embedded")) },
            { Assertions.assertNotNull(responseBody.getJsonObject("_embedded").getJsonArray("movies")) },
            {
                Assertions.assertNotNull(
                    responseBody.getJsonObject("_embedded").getJsonArray("movies").getJsonObject(0)
                )
            },
            {
                Assertions.assertNotNull(
                    responseBody.getJsonObject("_embedded").getJsonArray("movies").getJsonObject(0).getString("id")
                )
            },
            {
                Assertions.assertEquals(
                    movie.title,
                    responseBody.getJsonObject("_embedded").getJsonArray("movies").getJsonObject(0).getString("title")
                )
            },
            {
                Assertions.assertEquals(
                    movie.released,
                    responseBody.getJsonObject("_embedded").getJsonArray("movies").getJsonObject(0)
                        .getInteger("released")
                )
            },
            {
                Assertions.assertNotNull(
                    responseBody.getJsonObject("_embedded").getJsonArray("movies").getJsonObject(0)
                        .getString("created_at")
                )
            },
            {
                Assertions.assertNotNull(
                    responseBody.getJsonObject("_embedded").getJsonArray("movies").getJsonObject(0)
                        .getString("updated_at")
                )
            },
            { Assertions.assertNotNull(responseBody.getJsonObject("_links")) },
            { Assertions.assertNotNull(responseBody.getJsonObject("_links").getJsonObject("self")) },
            { Assertions.assertNotNull(responseBody.getJsonObject("_links").getJsonObject("first")) },
            { Assertions.assertNotNull(responseBody.getJsonObject("_links").getJsonObject("previous")) },
            { Assertions.assertNotNull(responseBody.getJsonObject("_links").getJsonObject("next")) },
            { Assertions.assertNotNull(responseBody.getJsonObject("_links").getJsonObject("last")) },
            { Assertions.assertNotNull(responseBody.getJsonObject("_links").getJsonObject("self").getString("href")) },
            { Assertions.assertNotNull(responseBody.getJsonObject("_links").getJsonObject("first").getString("href")) },
            {
                Assertions.assertNotNull(
                    responseBody.getJsonObject("_links").getJsonObject("previous").getString("href")
                )
            },
            { Assertions.assertNotNull(responseBody.getJsonObject("_links").getJsonObject("next").getString("href")) },
            { Assertions.assertNotNull(responseBody.getJsonObject("_links").getJsonObject("last").getString("href")) },
            {
                assertTrue(
                    responseBody.getJsonObject("_embedded").getJsonArray("movies").getJsonObject(0)
                        .getJsonObject("_links").getJsonObject("self").getString("href")
                        .contains("${MovieController.PATH}/${movie.id}")
                )
            },
        )
    }

    @Test
    @DisplayName("2 - Read Nonexistent Movie")
    fun testReadNonexistentMovie() {

        val responseBody = JsonObject(
            RestAssured
                .given()
                .headers(
                    Headers.headers(
                        Header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                    )
                )
                .`when`()
                .get("${MovieController.PATH}/61a66a521ec5f616a4fc2f9f")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .extract()
                .body()
                .asString()
        )

        assertAll(
            { Assertions.assertEquals("404", responseBody.getString("code")) },
            {
                Assertions.assertEquals(
                    "Movie with id '61a66a521ec5f616a4fc2f9f' does not exist",
                    responseBody.getString("message")
                )
            },
        )
    }

    @AfterAll
    fun teardown() {
        deleteMovie()
        deletePerson()
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
