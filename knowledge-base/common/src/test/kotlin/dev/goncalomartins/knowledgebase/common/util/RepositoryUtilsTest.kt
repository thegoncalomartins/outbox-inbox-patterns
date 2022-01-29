package dev.goncalomartins.knowledgebase.common.util

import dev.goncalomartins.knowledgebase.common.model.graph.Graph
import dev.goncalomartins.knowledgebase.common.model.movie.Movie
import dev.goncalomartins.knowledgebase.common.model.person.Person
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.neo4j.driver.Record
import org.neo4j.driver.internal.InternalNode
import org.neo4j.driver.internal.InternalPath
import org.neo4j.driver.internal.InternalRelationship
import org.neo4j.driver.internal.value.DateValue
import org.neo4j.driver.internal.value.IntegerValue
import org.neo4j.driver.internal.value.LocalDateTimeValue
import org.neo4j.driver.internal.value.StringValue
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RepositoryUtilsTest {

    @Test
    fun `graphPaths() - test with only one node`() {
        val graph = Graph()
        val record = mock(Record::class.java, RETURNS_DEEP_STUBS)
        val localDateTime = LocalDateTime.now()
        val personNode = InternalNode(
            1,
            listOf("Person"),
            mapOf(
                "id" to StringValue("id"),
                "name" to StringValue("name"),
                "birth_date" to DateValue(localDateTime.toLocalDate()),
                "created_at" to LocalDateTimeValue(localDateTime),
                "updated_at" to LocalDateTimeValue(localDateTime)
            )
        )

        val person = Person.fromNode(personNode)

        whenever(record["path"].isNull)
            .thenReturn(true)

        whenever(record["node"].asNode())
            .thenReturn(personNode)

        RepositoryUtils.graphPaths(graph).apply(record)

        assertAll(
            { assertFalse(graph.isEmpty()) },
            { assertEquals(1, graph.nodes().first().id) },
            { assertEquals("Person", graph.nodes().first().label) },
            { assertEquals(person, graph.nodes().first().metadata) },
        )
    }

    @Test
    fun `graphPaths() - test with more than one node`() {
        val graph = Graph()
        val record = mock(Record::class.java, RETURNS_DEEP_STUBS)
        val localDateTime = LocalDateTime.now()
        val personNode = InternalNode(
            1,
            listOf("Person"),
            mapOf(
                "id" to StringValue("id"),
                "name" to StringValue("name"),
                "birth_date" to DateValue(localDateTime.toLocalDate()),
                "created_at" to LocalDateTimeValue(localDateTime),
                "updated_at" to LocalDateTimeValue(localDateTime)
            )
        )

        val movieNode = InternalNode(
            2,
            listOf("Movie"),
            mapOf(
                "id" to StringValue("id"),
                "title" to StringValue("title"),
                "released" to IntegerValue(2022),
                "created_at" to LocalDateTimeValue(localDateTime),
                "updated_at" to LocalDateTimeValue(localDateTime)
            )
        )

        val relationship = InternalRelationship(1, 2, 1, "DIRECTED_BY")

        val path = InternalPath(
            listOf(InternalPath.SelfContainedSegment(movieNode, relationship, personNode)),
            listOf(movieNode, personNode),
            listOf(relationship)
        )

        val person = Person.fromNode(personNode)
        val movie = Movie.fromNode(movieNode)

        whenever(record["path"].isNull)
            .thenReturn(false)

        whenever(record["path"].asPath())
            .thenReturn(path)

        whenever(record["node"].asNode())
            .thenReturn(personNode)

        RepositoryUtils.graphPaths(graph).apply(record)

        assertAll(
            { assertFalse(graph.isEmpty()) },
            { assertEquals(2, graph.nodes().size) },
            { assertEquals(1, graph.edges().size) },
            {
                assertTrue(
                    graph.nodes().any { node -> node.id == 1L && node.label == "Person" && node.metadata == person }
                )
            },
            {
                assertTrue(
                    graph.nodes().any { node -> node.id == 2L && node.label == "Movie" && node.metadata == movie }
                )
            },
            {
                assertTrue(
                    graph.edges()
                        .any { edge -> edge.id == 1L && edge.from == 2L && edge.to == 1L && edge.relationship == "DIRECTED_BY" }
                )
            }
        )
    }

    @Test
    fun testPrepareQuery() {
        val query = """
                MATCH (person: Person)
                RETURN person ORDER BY person.updated_at DESC SKIP ${'$'}skip LIMIT ${'$'}limit
            """

        val params = mapOf("skip" to "0", "limit" to "20")

        val expected = """
                MATCH (person: Person)
                RETURN person ORDER BY person.updated_at DESC SKIP 0 LIMIT 20
            """

        val result = RepositoryUtils.prepareQuery(query, params)

        assertEquals(expected, result)
    }
}
