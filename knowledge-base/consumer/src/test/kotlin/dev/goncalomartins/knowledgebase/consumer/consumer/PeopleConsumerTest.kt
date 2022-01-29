package dev.goncalomartins.knowledgebase.consumer.consumer

import dev.goncalomartins.knowledgebase.common.model.person.Person
import dev.goncalomartins.knowledgebase.common.service.PersonService
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
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@QuarkusTest
@TestMethodOrder(MethodOrderer.DisplayName::class)
class PeopleConsumerTest {
    companion object {
        const val CREATE_PERSON_FILENAME = "createPerson.json"
        private const val UPDATE_PERSON_FILENAME = "updatePerson.json"
        private const val DELETE_PERSON_FILENAME = "deletePerson.json"
        private val DURATION: Duration = Duration.ofSeconds(3)
    }

    @Inject
    lateinit var personService: PersonService

    @Inject
    lateinit var testUtils: TestUtils

    private val personId = UUID.randomUUID().toString()

    @Test
    @DisplayName("1 - Test Person Creation")
    fun testPersonCreation() {
        val outboxEventId = UUID.randomUUID().toString()
        val payload = JsonObject(String.format(testUtils.readFileAsString(CREATE_PERSON_FILENAME), outboxEventId, personId, personId))
        val personPayload = JsonObject(payload.getString("payload"))
        val record = KafkaRecord.of(payload.getString("id"), payload.encode())

        testUtils.producePeopleMessage(record)

        await
            .atMost(Duration.ofMinutes(1)) // first test needs more time due to application launch
            .untilAsserted {
                val graph = personService.findOne(personPayload.getString("id"))
                    .await()
                    .indefinitely()

                assertAll(
                    { assertEquals(1, graph.total()) },
                    { assertEquals(1, graph.nodes().size) },
                    {
                        val node = graph.nodes().elementAt(0)

                        val person = node.metadata as Person

                        assertAll(
                            { assertEquals(personPayload.getString("id"), person.id) },
                            { assertEquals(personPayload.getString("name"), person.name) },
                            { assertEquals(LocalDate.parse(personPayload.getString("birth_date")), person.birthDate) },
                            { assertEquals(Instant.parse(personPayload.getString("created_at")), person.createdAt) },
                            { assertEquals(Instant.parse(personPayload.getString("updated_at")), person.updatedAt) },
                        )
                    },
                )
            }
    }

    @Test
    @DisplayName("2 - Test Person Update")
    fun testPersonUpdate() {
        val outboxEventId = UUID.randomUUID().toString()
        val payload = JsonObject(String.format(testUtils.readFileAsString(UPDATE_PERSON_FILENAME), outboxEventId, personId, personId))
        val personPayload = JsonObject(payload.getString("payload"))
        val record = KafkaRecord.of(payload.getString("id"), payload.encode())

        testUtils.producePeopleMessage(record)

        await
            .atMost(DURATION)
            .untilAsserted {
                val graph = personService.findOne(personPayload.getString("id"))
                    .await()
                    .indefinitely()

                assertAll(
                    { assertEquals(1, graph.total()) },
                    { assertEquals(1, graph.nodes().size) },
                    {
                        val node = graph.nodes().elementAt(0)

                        val person = node.metadata as Person

                        assertAll(
                            { assertEquals(personPayload.getString("id"), person.id) },
                            { assertEquals(personPayload.getString("name"), person.name) },
                            { assertEquals(LocalDate.parse(personPayload.getString("birth_date")), person.birthDate) },
                            { assertEquals(Instant.parse(personPayload.getString("created_at")), person.createdAt) },
                            { assertEquals(Instant.parse(personPayload.getString("updated_at")), person.updatedAt) },
                        )
                    },
                )
            }
    }

    @Test
    @DisplayName("3 - Test Person Deletion")
    fun testPersonDeletion() {
        val outboxEventId = UUID.randomUUID().toString()
        val payload = JsonObject(String.format(testUtils.readFileAsString(DELETE_PERSON_FILENAME), outboxEventId, personId, personId))
        val personPayload = JsonObject(payload.getString("payload"))
        val record = KafkaRecord.of(payload.getString("id"), payload.encode())

        testUtils.producePeopleMessage(record)

        await
            .atMost(DURATION)
            .untilAsserted {
                val graph = personService.findOne(personPayload.getString("id"))
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
        testPersonDeletion()
    }
}
