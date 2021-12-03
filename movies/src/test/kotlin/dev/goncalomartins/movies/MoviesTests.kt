package dev.goncalomartins.movies

import dev.goncalomartins.movies.controller.MovieController
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.Header
import io.restassured.http.Headers
import io.vertx.core.json.JsonObject
import org.apache.http.HttpHeaders
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.assertAll
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors
import javax.ws.rs.core.MediaType

@Suppress("UNCHECKED_CAST")
@QuarkusTest
@TestMethodOrder(MethodOrderer.DisplayName::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MoviesTests {

    private companion object {
        const val CREATE_MOVIE_FILENAME = "createMovie.json"
        const val UPDATE_MOVIE_FILENAME = "updateMovie.json"
    }

    private lateinit var movieId: String

    @Test
    @DisplayName("1 - Create Movie")
    fun testCreateMovie() {
        val requestBody = JsonObject(readFileAsString(CREATE_MOVIE_FILENAME))

        val responseBody = JsonObject(
            RestAssured
                .given()
                .headers(
                    Headers.headers(
                        Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON),
                        Header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                    )
                )
                .body(requestBody.encode())
                .`when`()
                .post(MovieController.PATH)
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .body()
                .asString()
        )

        movieId = responseBody.getString("id")

        assertAll(
            { Assertions.assertEquals(requestBody.getString("title"), responseBody.getString("title")) },
            { Assertions.assertEquals(requestBody.getInteger("released"), responseBody.getInteger("released")) },
            {
                Assertions.assertEquals(
                    requestBody.getJsonArray("directed_by"),
                    responseBody.getJsonArray("directed_by")
                )
            },
            { Assertions.assertEquals(requestBody.getJsonArray("cast").list.map { it as LinkedHashMap<String, String> }.sortedBy { it["person_id"] }, responseBody.getJsonArray("cast").list.map { it as LinkedHashMap<String, String> }.sortedBy { it["person_id"] }) },
            { Assertions.assertNotNull(responseBody.getString("created_at")) },
            { Assertions.assertNotNull(responseBody.getString("updated_at")) },
            { Assertions.assertNotNull(responseBody.getJsonObject("_links")) },
            { Assertions.assertNotNull(responseBody.getJsonObject("_links").getJsonObject("self")) },
            { Assertions.assertNotNull(responseBody.getJsonObject("_links").getJsonObject("self").getString("href")) },
            {
                assertTrue(
                    responseBody.getJsonObject("_links").getJsonObject("self").getString("href")
                        .contains("${MovieController.PATH}/$movieId")
                )
            },
        )
    }

    @Test
    @DisplayName("2 - List Movies")
    fun testListMovies() {
        val movie = JsonObject(readFileAsString(CREATE_MOVIE_FILENAME))

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
                    movie.getString("title"),
                    responseBody.getJsonObject("_embedded").getJsonArray("movies").getJsonObject(0).getString("title")
                )
            },
            {
                Assertions.assertEquals(
                    movie.getInteger("released"),
                    responseBody.getJsonObject("_embedded").getJsonArray("movies").getJsonObject(0)
                        .getInteger("released")
                )
            },
            {
                Assertions.assertEquals(
                    movie.getJsonArray("directed_by"),
                    responseBody.getJsonObject("_embedded").getJsonArray("movies").getJsonObject(0)
                        .getJsonArray("directed_by")
                )
            },
            {
                Assertions.assertEquals(
                    movie.getJsonArray("cast").list.map { it as LinkedHashMap<String, String> }.sortedBy { it["person_id"] },
                    responseBody.getJsonObject("_embedded").getJsonArray("movies").getJsonObject(0)
                        .getJsonArray("cast").list.map { it as LinkedHashMap<String, String> }.sortedBy { it["person_id"] }
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
                        .contains("${MovieController.PATH}/$movieId")
                )
            },
        )
    }

    @Test
    @DisplayName("3 - Read Movie")
    fun testReadMovie() {
        val movie = JsonObject(readFileAsString(CREATE_MOVIE_FILENAME))

        val responseBody = JsonObject(
            RestAssured
                .given()
                .headers(
                    Headers.headers(
                        Header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                    )
                )
                .`when`()
                .get("${MovieController.PATH}/$movieId")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .body()
                .asString()
        )

        assertAll(
            { Assertions.assertEquals(movie.getString("title"), responseBody.getString("title")) },
            { Assertions.assertEquals(movie.getInteger("released"), responseBody.getInteger("released")) },
            {
                Assertions.assertEquals(
                    movie.getJsonArray("directed_by"),
                    responseBody.getJsonArray("directed_by")
                )
            },
            { Assertions.assertEquals(movie.getJsonArray("cast").list.map { it as LinkedHashMap<String, String> }.sortedBy { it["person_id"] }, responseBody.getJsonArray("cast").list.map { it as LinkedHashMap<String, String> }.sortedBy { it["person_id"] }) },
            { Assertions.assertNotNull(responseBody.getString("created_at")) },
            { Assertions.assertNotNull(responseBody.getString("updated_at")) },
            { Assertions.assertNotNull(responseBody.getJsonObject("_links")) },
            { Assertions.assertNotNull(responseBody.getJsonObject("_links").getJsonObject("self")) },
            { Assertions.assertNotNull(responseBody.getJsonObject("_links").getJsonObject("self").getString("href")) },
            {
                assertTrue(
                    responseBody.getJsonObject("_links").getJsonObject("self").getString("href")
                        .contains("${MovieController.PATH}/$movieId")
                )
            },
        )
    }

    @Test
    @DisplayName("3.1 - Read Nonexistent Movie")
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

    @Test
    @DisplayName("4 - Update Movie")
    fun testUpdateMovie() {
        val requestBody = JsonObject(readFileAsString(UPDATE_MOVIE_FILENAME))

        val responseBody = JsonObject(
            RestAssured
                .given()
                .headers(
                    Headers.headers(
                        Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON),
                        Header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                    )
                )
                .body(requestBody.encode())
                .`when`()
                .put("${MovieController.PATH}/$movieId")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .body()
                .asString()
        )

        assertAll(
            { Assertions.assertNotNull(responseBody.getString("id")) },
            { Assertions.assertEquals(requestBody.getString("title"), responseBody.getString("title")) },
            { Assertions.assertEquals(requestBody.getInteger("released"), responseBody.getInteger("released")) },
            {
                Assertions.assertEquals(
                    requestBody.getJsonArray("directed_by"),
                    responseBody.getJsonArray("directed_by")
                )
            },
            { Assertions.assertEquals(requestBody.getJsonArray("cast").list.map { it as LinkedHashMap<String, String> }.sortedBy { it["person_id"] }, responseBody.getJsonArray("cast").list.map { it as LinkedHashMap<String, String> }.sortedBy { it["person_id"] }) },
            { Assertions.assertNotNull(responseBody.getString("created_at")) },
            { Assertions.assertNotNull(responseBody.getString("updated_at")) },
            { Assertions.assertNotNull(responseBody.getJsonObject("_links")) },
            { Assertions.assertNotNull(responseBody.getJsonObject("_links").getJsonObject("self")) },
            { Assertions.assertNotNull(responseBody.getJsonObject("_links").getJsonObject("self").getString("href")) },
            {
                assertTrue(
                    responseBody.getJsonObject("_links").getJsonObject("self").getString("href")
                        .contains("${MovieController.PATH}/$movieId")
                )
            },
        )
    }

    @Test
    @DisplayName("4.1 - Update Nonexistent Movie")
    fun testUpdateNonexistentMovie() {
        val requestBody = JsonObject(readFileAsString(UPDATE_MOVIE_FILENAME))

        val responseBody = JsonObject(
            RestAssured
                .given()
                .headers(
                    Headers.headers(
                        Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON),
                        Header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                    )
                )
                .body(requestBody.encode())
                .`when`()
                .put("${MovieController.PATH}/61a66a521ec5f616a4fc2f9f")
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

    @Test
    @DisplayName("5 - Delete Movie")
    fun testDeleteMovie() {
        RestAssured
            .given()
            .headers(
                Headers.headers(
                    Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON),
                    Header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                )
            )
            .`when`()
            .delete("${MovieController.PATH}/$movieId")
            .then()
            .statusCode(HttpStatus.SC_NO_CONTENT)
    }

    @Test
    @DisplayName("5.1 - Delete Nonexistent Movie")
    fun testDeleteNonexistentMovie() {

        val responseBody = JsonObject(
            RestAssured
                .given()
                .headers(
                    Headers.headers(
                        Header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                    )
                )
                .`when`()
                .delete("${MovieController.PATH}/61a66a521ec5f616a4fc2f9f")
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

    private fun readFileAsString(file: String): String {
        val resource: InputStream = MoviesTests::class.java.classLoader.getResourceAsStream(file)!!

        return BufferedReader(InputStreamReader(resource, StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.joining("\n"))
    }
}
