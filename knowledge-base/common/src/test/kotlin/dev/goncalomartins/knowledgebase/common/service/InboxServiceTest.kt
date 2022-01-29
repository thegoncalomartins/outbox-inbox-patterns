package dev.goncalomartins.knowledgebase.common.service

import dev.goncalomartins.knowledgebase.common.model.inbox.InboxEvent
import dev.goncalomartins.knowledgebase.common.repository.InboxRepository
import io.smallrye.mutiny.Uni
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.neo4j.driver.reactive.RxTransaction

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InboxServiceTest {

    private lateinit var inboxRepository: InboxRepository

    private lateinit var subject: InboxService

    @BeforeEach
    fun setup() {
        inboxRepository = mock(InboxRepository::class.java)
        subject = InboxService(inboxRepository)
    }

    @Test
    fun testSave() {
        val transaction = mock(RxTransaction::class.java)

        val inboxEvent = InboxEvent()

        val uni = Uni.createFrom().voidItem()

        whenever(inboxRepository.save(transaction, inboxEvent))
            .thenReturn(uni)

        subject.save(transaction, inboxEvent).await().indefinitely()

        verify(inboxRepository, times(1)).save(transaction, inboxEvent)
    }

    @Test
    fun testFindOne() {
        val transaction = mock(RxTransaction::class.java)

        val id = "id"

        val inboxEvent = InboxEvent()

        val uni = Uni.createFrom().item(inboxEvent)

        whenever(inboxRepository.findOne(transaction, id))
            .thenReturn(uni)

        assertEquals(inboxEvent, subject.findOne(transaction, id).await().indefinitely())

        verify(inboxRepository, times(1)).findOne(transaction, id)
    }
}
