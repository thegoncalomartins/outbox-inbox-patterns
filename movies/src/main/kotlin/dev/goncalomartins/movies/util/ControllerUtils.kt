package dev.goncalomartins.movies.util

import dev.goncalomartins.movies.dto.hypermedia.Link
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.net.URI
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.core.UriBuilder

@ApplicationScoped
class ControllerUtils(
    @ConfigProperty(name = "api.gateway.uri") val apiGatewayURI: URI
) {
    /**
     * Build hypermedia link
     *
     * @param path the endpoint route
     * @param queryParams map containing the query parameters
     * @param uriVariables map containing the URI variables.
     * @return a [Link] instance representing the hypermedia link that was built
     */
    fun buildLink(
        path: String,
        queryParams: Map<String, String> = mapOf(),
        uriVariables: Map<String, String> = mapOf()
    ): Link = Link(
        UriBuilder.fromUri(apiGatewayURI)
            .path(path)
            .apply {
                queryParams.forEach { (key, value) -> queryParam(key, value) }
            }.buildFromMap(uriVariables)
    )

    fun calculateFirst(limit: Int) = mapOf("limit" to limit.toString(), "skip" to "0")

    fun calculatePrevious(limit: Int, skip: Int) =
        mapOf("limit" to limit.toString(), "skip" to (skip - limit).coerceAtLeast(0).toString())

    fun calculateNext(total: Long, limit: Int, skip: Int) =
        mapOf("limit" to limit.toString(), "skip" to (skip + limit).coerceAtMost(total.toInt()).toString())

    fun calculateLast(total: Long, limit: Int) =
        mapOf("limit" to limit.toString(), "skip" to (total - limit).coerceAtLeast(0).toString())
}
