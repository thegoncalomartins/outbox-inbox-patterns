package dev.goncalomartins.people.controller

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.Header
import io.restassured.http.Headers
import io.vertx.core.json.JsonObject
import org.apache.http.HttpHeaders
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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

@QuarkusTest
@TestMethodOrder(MethodOrderer.DisplayName::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PersonControllerTest {
    private companion object {
        const val CREATE_PERSON_FILENAME = "createPerson.json"
        const val UPDATE_PERSON_FILENAME = "updatePerson.json"
    }

    private lateinit var personId: String

    @Test
    @DisplayName("1 - Create Person")
    fun testCreatePerson() {
        val requestBody = JsonObject(readFileAsString(CREATE_PERSON_FILENAME))

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
                .post(PersonController.PATH)
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .body()
                .asString()
        )

        personId = responseBody.getString("id")

        assertAll(
            { assertEquals(requestBody.getString("name"), responseBody.getString("name")) },
            { assertEquals(requestBody.getString("birth_date"), responseBody.getString("birth_date")) },
            { assertNotNull(responseBody.getString("created_at")) },
            { assertNotNull(responseBody.getString("updated_at")) },
            { assertNotNull(responseBody.getJsonObject("_links")) },
            { assertNotNull(responseBody.getJsonObject("_links").getJsonObject("self")) },
            { assertNotNull(responseBody.getJsonObject("_links").getJsonObject("self").getString("href")) },
            {
                assertTrue(
                    responseBody.getJsonObject("_links").getJsonObject("self").getString("href")
                        .contains("${PersonController.PATH}/$personId")
                )
            },
        )
    }

    @Test
    @DisplayName("2 - List People")
    fun testListPeople() {
        val person = JsonObject(readFileAsString(CREATE_PERSON_FILENAME))

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
                    person.getString("name"),
                    responseBody.getJsonObject("_embedded").getJsonArray("people").getJsonObject(0).getString("name")
                )
            },
            {
                assertEquals(
                    person.getString("birth_date"),
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
                        .contains("${PersonController.PATH}/$personId")
                )
            },
        )
    }

    @Test
    @DisplayName("3 - Read Person")
    fun testReadPerson() {
        val person = JsonObject(readFileAsString(CREATE_PERSON_FILENAME))

        val responseBody = JsonObject(
            RestAssured
                .given()
                .headers(
                    Headers.headers(
                        Header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                    )
                )
                .`when`()
                .get("${PersonController.PATH}/$personId")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .body()
                .asString()
        )

        assertAll(
            { assertEquals(person.getString("name"), responseBody.getString("name")) },
            { assertEquals(person.getString("birth_date"), responseBody.getString("birth_date")) },
            { assertNotNull(responseBody.getString("created_at")) },
            { assertNotNull(responseBody.getString("updated_at")) },
            { assertNotNull(responseBody.getJsonObject("_links")) },
            { assertNotNull(responseBody.getJsonObject("_links").getJsonObject("self")) },
            { assertNotNull(responseBody.getJsonObject("_links").getJsonObject("self").getString("href")) },
            {
                assertTrue(
                    responseBody.getJsonObject("_links").getJsonObject("self").getString("href")
                        .contains("${PersonController.PATH}/$personId")
                )
            },
        )
    }

    @Test
    @DisplayName("3.1 - Read Nonexistent Person")
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
    @DisplayName("4 - Update Person")
    fun testUpdatePerson() {
        val requestBody = JsonObject(readFileAsString(UPDATE_PERSON_FILENAME))

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
                .put("${PersonController.PATH}/$personId")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .body()
                .asString()
        )

        assertAll(
            { assertNotNull(responseBody.getString("id")) },
            { assertEquals(requestBody.getString("name"), responseBody.getString("name")) },
            { assertEquals(requestBody.getString("birth_date"), responseBody.getString("birth_date")) },
            { assertNotNull(responseBody.getString("created_at")) },
            { assertNotNull(responseBody.getString("updated_at")) },
            { assertNotNull(responseBody.getJsonObject("_links")) },
            { assertNotNull(responseBody.getJsonObject("_links").getJsonObject("self")) },
            { assertNotNull(responseBody.getJsonObject("_links").getJsonObject("self").getString("href")) },
            {
                assertTrue(
                    responseBody.getJsonObject("_links").getJsonObject("self").getString("href")
                        .contains("${PersonController.PATH}/$personId")
                )
            },
        )
    }

    @Test
    @DisplayName("4.1 - Update Nonexistent Person")
    fun testUpdateNonexistentPerson() {
        val requestBody = JsonObject(readFileAsString(UPDATE_PERSON_FILENAME))

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
                .put("${PersonController.PATH}/61a66a521ec5f616a4fc2f9f")
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
    @DisplayName("5 - Delete Person")
    fun testDeletePerson() {
        RestAssured
            .given()
            .headers(
                Headers.headers(
                    Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON),
                    Header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                )
            )
            .`when`()
            .delete("${PersonController.PATH}/$personId")
            .then()
            .statusCode(HttpStatus.SC_NO_CONTENT)
    }

    @Test
    @DisplayName("5.1 - Delete Nonexistent Person")
    fun testDeleteNonexistentPerson() {

        val responseBody = JsonObject(
            RestAssured
                .given()
                .headers(
                    Headers.headers(
                        Header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                    )
                )
                .`when`()
                .delete("${PersonController.PATH}/61a66a521ec5f616a4fc2f9f")
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

    private fun readFileAsString(file: String): String {
        val resource: InputStream = PersonControllerTest::class.java.classLoader.getResourceAsStream(file)!!

        return BufferedReader(InputStreamReader(resource, StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.joining("\n"))
    }
}
