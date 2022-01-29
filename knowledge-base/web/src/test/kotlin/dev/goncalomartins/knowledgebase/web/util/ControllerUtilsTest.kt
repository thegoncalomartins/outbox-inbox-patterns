package dev.goncalomartins.knowledgebase.web.util

import io.vertx.ext.web.RoutingContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.net.InetAddress
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ControllerUtilsTest {

    private lateinit var controllerUtils: ControllerUtils
    private lateinit var context: RoutingContext
    private val port = 8080

    @BeforeAll
    fun setup() {
        controllerUtils = ControllerUtils(port)
        context = mock(RoutingContext::class.java, RETURNS_DEEP_STUBS)
    }

    @ParameterizedTest
    @MethodSource(value = ["buildLinkDataProvider"])
    fun testBuildLink(
        xForwardedFor: String?,
        xForwardedProto: String?,
        path: String,
        queryParams: Map<String, String>,
        uriVariables: Map<String, String>,
        expected: String
    ) {
        whenever(context.request().getHeader("X-Forwarded-For"))
            .thenReturn(xForwardedFor)

        whenever(context.request().getHeader("X-Forwarded-Proto"))
            .thenReturn(xForwardedProto)

        val result = controllerUtils.buildLink(context, path, queryParams, uriVariables).href.toString()

        assertEquals(expected, result)
    }

    @Test
    fun testCalculateFirst() {
        val limit = 10
        val result = controllerUtils.calculateFirst(limit)

        val expected = mapOf("limit" to limit.toString(), "skip" to "0")
        assertEquals(expected, result)
    }

    @ParameterizedTest
    @MethodSource(value = ["calculatePreviousDataProvider"])
    fun testCalculatePrevious(limit: Int, skip: Int, expected: Map<String, String>) {
        val result = controllerUtils.calculatePrevious(limit, skip)

        assertEquals(expected, result)
    }

    @ParameterizedTest
    @MethodSource(value = ["calculateNextDataProvider"])
    fun testCalculateNext(total: Long, limit: Int, skip: Int, expected: Map<String, String>) {
        val result = controllerUtils.calculateNext(total, limit, skip)

        assertEquals(expected, result)
    }

    @ParameterizedTest
    @MethodSource(value = ["calculateLastDataProvider"])
    fun testCalculateLast(total: Long, limit: Int, expected: Map<String, String>) {
        val result = controllerUtils.calculateLast(total, limit)

        assertEquals(expected, result)
    }

    private fun buildLinkDataProvider(): Stream<Arguments> = Stream.of(
        Arguments.arguments(
            "goncalomartins.dev",
            "http",
            "/api/knowledge-base/people/{id}",
            mapOf("limit" to "10", "skip" to "0"),
            mapOf("id" to "123456"),
            "http://goncalomartins.dev/api/knowledge-base/people/123456?limit=10&skip=0"
        ),
        Arguments.arguments(
            "goncalomartins.dev",
            "http",
            "/api/knowledge-base/people",
            mapOf<String, String>(),
            mapOf<String, String>(),
            "http://goncalomartins.dev/api/knowledge-base/people"
        ),
        Arguments.arguments(
            "goncalomartins.dev",
            "http",
            "/api/knowledge-base/people",
            mapOf("limit" to "10", "skip" to "0"),
            mapOf<String, String>(),
            "http://goncalomartins.dev/api/knowledge-base/people?limit=10&skip=0"
        ),
        Arguments.arguments(
            null,
            "http",
            "/api/knowledge-base/people/{id}",
            mapOf("limit" to "10", "skip" to "0"),
            mapOf("id" to "123456"),
            "http://${InetAddress.getLocalHost().hostAddress}:$port/api/knowledge-base/people/123456?limit=10&skip=0"
        ),
        Arguments.arguments(
            null,
            "http",
            "/api/knowledge-base/people",
            mapOf<String, String>(),
            mapOf<String, String>(),
            "http://${InetAddress.getLocalHost().hostAddress}:$port/api/knowledge-base/people"
        ),
        Arguments.arguments(
            null,
            "http",
            "/api/knowledge-base/people",
            mapOf("limit" to "10", "skip" to "0"),
            mapOf<String, String>(),
            "http://${InetAddress.getLocalHost().hostAddress}:$port/api/knowledge-base/people?limit=10&skip=0"
        ),
        Arguments.arguments(
            null,
            null,
            "/api/knowledge-base/people",
            mapOf("limit" to "10", "skip" to "0"),
            mapOf<String, String>(),
            "http://${InetAddress.getLocalHost().hostAddress}:$port/api/knowledge-base/people?limit=10&skip=0"
        ),
        Arguments.arguments(
            "goncalomartins.dev",
            "https",
            "/api/knowledge-base/people/{id}",
            mapOf("limit" to "10", "skip" to "0"),
            mapOf("id" to "123456"),
            "https://goncalomartins.dev/api/knowledge-base/people/123456?limit=10&skip=0"
        ),
        Arguments.arguments(
            "goncalomartins.dev",
            "https",
            "/api/knowledge-base/people",
            mapOf<String, String>(),
            mapOf<String, String>(),
            "https://goncalomartins.dev/api/knowledge-base/people"
        ),
        Arguments.arguments(
            "goncalomartins.dev",
            "https",
            "/api/knowledge-base/people",
            mapOf("limit" to "10", "skip" to "0"),
            mapOf<String, String>(),
            "https://goncalomartins.dev/api/knowledge-base/people?limit=10&skip=0"
        ),
        Arguments.arguments(
            null,
            "https",
            "/api/knowledge-base/people/{id}",
            mapOf("limit" to "10", "skip" to "0"),
            mapOf("id" to "123456"),
            "https://${InetAddress.getLocalHost().hostAddress}:$port/api/knowledge-base/people/123456?limit=10&skip=0"
        ),
        Arguments.arguments(
            null,
            "https",
            "/api/knowledge-base/people",
            mapOf<String, String>(),
            mapOf<String, String>(),
            "https://${InetAddress.getLocalHost().hostAddress}:$port/api/knowledge-base/people"
        ),
        Arguments.arguments(
            null,
            "https",
            "/api/knowledge-base/people",
            mapOf("limit" to "10", "skip" to "0"),
            mapOf<String, String>(),
            "https://${InetAddress.getLocalHost().hostAddress}:$port/api/knowledge-base/people?limit=10&skip=0"
        )
    )

    private fun calculatePreviousDataProvider(): Stream<Arguments> =
        Stream.of(
            Arguments.arguments(
                10,
                10,
                mapOf("limit" to "10", "skip" to "0")
            ),
            Arguments.arguments(
                10,
                20,
                mapOf("limit" to "10", "skip" to "10")
            ),
            Arguments.arguments(
                10,
                2,
                mapOf("limit" to "10", "skip" to "0")
            )
        )

    private fun calculateNextDataProvider(): Stream<Arguments> =
        Stream.of(
            Arguments.arguments(
                50,
                10,
                10,
                mapOf("limit" to "10", "skip" to "20")
            ),
            Arguments.arguments(
                50,
                10,
                40,
                mapOf("limit" to "10", "skip" to "50")
            ),
            Arguments.arguments(
                50,
                10,
                50,
                mapOf("limit" to "10", "skip" to "50")
            )
        )

    private fun calculateLastDataProvider(): Stream<Arguments> =
        Stream.of(
            Arguments.arguments(
                50,
                10,
                mapOf("limit" to "10", "skip" to "40")
            ),
            Arguments.arguments(
                50,
                50,
                mapOf("limit" to "50", "skip" to "0")
            ),
            Arguments.arguments(
                50,
                70,
                mapOf("limit" to "70", "skip" to "0")
            )
        )
}
