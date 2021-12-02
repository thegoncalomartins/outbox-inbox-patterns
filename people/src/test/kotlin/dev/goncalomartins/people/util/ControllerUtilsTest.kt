package dev.goncalomartins.people.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.net.URI
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ControllerUtilsTest {

    private lateinit var controllerUtils: ControllerUtils
    private val apiGatewayURI = URI.create("https://goncalomartins.dev")

    @BeforeAll
    fun setup() = run { controllerUtils = ControllerUtils(apiGatewayURI) }

    @ParameterizedTest
    @MethodSource(value = ["buildLinkDataProvider"])
    fun testBuildLink(
        path: String,
        queryParams: Map<String, String>,
        uriVariables: Map<String, String>,
        expected: String
    ) {
        val result = controllerUtils.buildLink(path, queryParams, uriVariables).href.toString()

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
            "/api/people/{id}",
            mapOf("limit" to "10", "skip" to "0"),
            mapOf("id" to "123456"),
            "https://goncalomartins.dev/api/people/123456?limit=10&skip=0"
        ),
        Arguments.arguments(
            "/api/people",
            mapOf<String, String>(),
            mapOf<String, String>(),
            "https://goncalomartins.dev/api/people"
        ),
        Arguments.arguments(
            "/api/people",
            mapOf("limit" to "10", "skip" to "0"),
            mapOf<String, String>(),
            "https://goncalomartins.dev/api/people?limit=10&skip=0"
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
