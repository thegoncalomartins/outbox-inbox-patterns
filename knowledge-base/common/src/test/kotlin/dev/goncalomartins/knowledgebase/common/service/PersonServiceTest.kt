package dev.goncalomartins.knowledgebase.common.service

import dev.goncalomartins.knowledgebase.common.model.graph.Graph
import dev.goncalomartins.knowledgebase.common.model.person.People
import dev.goncalomartins.knowledgebase.common.model.person.Person
import dev.goncalomartins.knowledgebase.common.repository.PersonRepository
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
class PersonServiceTest {

    private lateinit var personRepository: PersonRepository

    private lateinit var databaseUtils: DatabaseUtils

    private lateinit var subject: PersonService

    @BeforeEach
    fun setup() {
        personRepository = mock(PersonRepository::class.java)
        databaseUtils = mock(DatabaseUtils::class.java)
        subject = PersonService(personRepository, databaseUtils)
    }

    @Test
    fun testFindOne() {
        val transaction = mock(RxTransaction::class.java)

        val graph = Graph()
        val id = "id"

        whenever(databaseUtils.inTransaction(any() as (RxTransaction) -> Uni<Graph>))
            .then { it.getArgument<(RxTransaction) -> Uni<Graph>>(0).invoke(transaction) }

        whenever(
            personRepository.findOne(
                transaction,
                id,
                PersonService.DEFAULT_SKIP,
                PersonService.DEFAULT_LIMIT
            )
        ).thenReturn(Uni.createFrom().item(graph))

        assertEquals(graph, subject.findOne(id).await().indefinitely())

        verify(personRepository, times(1))
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

        val people = People(0, emptyList())

        whenever(databaseUtils.inTransaction(any() as (RxTransaction) -> Uni<Graph>))
            .then { it.getArgument<(RxTransaction) -> Uni<Graph>>(0).invoke(transaction) }

        whenever(
            personRepository.count(
                transaction
            )
        ).thenReturn(Uni.createFrom().item(0L))

        whenever(
            personRepository.findAll(
                transaction,
                PersonService.DEFAULT_SKIP,
                PersonService.DEFAULT_LIMIT
            )
        ).thenReturn(Multi.createFrom().empty())

        assertEquals(people, subject.findAll().await().indefinitely())

        verify(personRepository, times(1))
            .count(transaction)

        verify(personRepository, times(1))
            .findAll(transaction, PersonService.DEFAULT_SKIP, PersonService.DEFAULT_LIMIT)
    }

    @Test
    fun testSave() {
        val transaction = mock(RxTransaction::class.java)

        val person = Person("id", "name", null, null, null)

        val uni = Uni.createFrom().voidItem()

        whenever(personRepository.save(transaction, person))
            .thenReturn(uni)

        subject.save(transaction, person).await().indefinitely()

        verify(personRepository, times(1)).save(transaction, person)
    }

    @Test
    fun testDelete() {
        val transaction = mock(RxTransaction::class.java)

        val person = Person("id", "name", null, null, null)

        val uni = Uni.createFrom().voidItem()

        whenever(personRepository.delete(transaction, person))
            .thenReturn(uni)

        subject.delete(transaction, person).await().indefinitely()

        verify(personRepository, times(1)).delete(transaction, person)
    }
}
