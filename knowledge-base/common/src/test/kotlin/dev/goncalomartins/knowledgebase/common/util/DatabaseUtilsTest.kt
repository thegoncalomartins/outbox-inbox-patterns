package dev.goncalomartins.knowledgebase.common.util

import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.neo4j.driver.Driver
import org.neo4j.driver.reactive.RxTransaction
import java.util.stream.Stream

@ExtendWith(MockitoExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseUtilsTest {

    companion object {
        @JvmStatic
        fun inTransactionProvider(): Stream<Arguments> = Stream.of(
            Arguments.of({ tx: RxTransaction -> Uni.createFrom().voidItem() }, true),
            Arguments.of({ tx: RxTransaction -> Uni.createFrom().failure<Void>(RuntimeException("Error")) }, false)
        )
    }

    private lateinit var subject: DatabaseUtils

    private lateinit var driver: Driver

    @BeforeEach
    fun setup() {
        driver = mock(Driver::class.java, Mockito.RETURNS_DEEP_STUBS)
        subject = DatabaseUtils(driver)
    }

    @ParameterizedTest
    @MethodSource("inTransactionProvider")
    fun testInTransaction(uni: (tx: RxTransaction) -> Uni<Void>, success: Boolean) {
        val session = driver.rxSession()

        val transaction = mock(RxTransaction::class.java, Mockito.RETURNS_DEEP_STUBS)

        val transactionPublisher = Multi.createFrom().item(transaction)

        whenever(session.beginTransaction())
            .thenReturn(transactionPublisher)

        whenever(transaction.commit<Void>())
            .thenReturn(Uni.createFrom().voidItem().toMulti())

        whenever(transaction.rollback<Void>())
            .thenReturn(Uni.createFrom().voidItem().toMulti())

        whenever(session.close<Void>())
            .thenReturn(Uni.createFrom().voidItem().toMulti())

        if (success) {
            subject.inTransaction(uni).await().indefinitely()
            verify(transaction, times(1)).commit<Void>()
        } else {
            assertThrows<Exception> {
                subject.inTransaction(uni).await().indefinitely()
            }
            verify(transaction, times(1)).rollback<Void>()
        }

        verify(session, times(1)).close<Void>()
    }
}
