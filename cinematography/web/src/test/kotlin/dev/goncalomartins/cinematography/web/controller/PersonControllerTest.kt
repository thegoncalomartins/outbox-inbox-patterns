package dev.goncalomartins.cinematography.web.controller

import dev.goncalomartins.cinematography.common.model.person.Person
import dev.goncalomartins.cinematography.common.service.PersonService
import dev.goncalomartins.cinematography.common.util.DatabaseUtils
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured
import io.restassured.http.Header
import io.restassured.http.Headers
import io.vertx.core.json.JsonObject
import org.apache.http.HttpStatus
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
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
class PersonControllerTest {

    @Inject
    lateinit var personService: PersonService

    @Inject
    lateinit var databaseUtils: DatabaseUtils

    val person = Person(UUID.randomUUID().toString(), "name", LocalDate.now(), Instant.now(), Instant.now())

    @BeforeAll
    fun setup() {
        createPerson()
    }

    @Test
    @DisplayName("2 - Read Nonexistent Person")
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
                .get("${PersonController.PATH}/61a66a521ec5f616a4fc2f9f")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .extract()
                .body()
                .asString()
        )

        Assertions.assertAll(
            { Assertions.assertEquals("404", responseBody.getString("code")) },
            {
                Assertions.assertEquals(
                    "Person with id '61a66a521ec5f616a4fc2f9f' does not exist",
                    responseBody.getString("message")
                )
            },
        )
    }

    @AfterAll
    fun teardown() {
        deletePerson()
    }

    private fun createPerson() {
        databaseUtils.inTransaction { transaction ->
            personService.save(transaction, person)
        }.await().indefinitely()
    }

    private fun deletePerson() {
        databaseUtils.inTransaction { transaction ->
            personService.delete(transaction, person)
        }.await().indefinitely()
    }
}
