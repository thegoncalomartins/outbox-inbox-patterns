package dev.goncalomartins.knowledgebase.web.util

import dev.goncalomartins.knowledgebase.common.model.graph.Graph
import dev.goncalomartins.knowledgebase.web.dto.graph.toDto
import dev.goncalomartins.knowledgebase.web.dto.hypermedia.CollectionDto
import dev.goncalomartins.knowledgebase.web.dto.hypermedia.Link
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

    fun toDto(
        path: String,
        id: String,
        total: Long,
        limit: Int,
        skip: Int,
        graph: dev.goncalomartins.knowledgebase.common.model.graph.Graph
    ) =
        CollectionDto(
            embedded = mapOf("graph" to graph.toDto()),
            links = mapOf(
                "first" to buildLink(
                    path = path,
                    uriVariables = mapOf("id" to id),
                    queryParams = calculateFirst(limit)
                ),
                "previous" to buildLink(
                    path = path,
                    uriVariables = mapOf("id" to id),
                    queryParams = calculatePrevious(limit = limit, skip = skip)
                ),
                "self" to buildLink(
                    path = path,
                    uriVariables = mapOf("id" to id),
                    queryParams = mapOf("limit" to limit.toString(), "skip" to skip.toString())
                ),
                "next" to buildLink(
                    path = path,
                    uriVariables = mapOf("id" to id),
                    queryParams = calculateNext(total = total, limit = limit, skip = skip)
                ),
                "last" to buildLink(
                    path = path,
                    uriVariables = mapOf("id" to id),
                    queryParams = calculateLast(total = total, limit = limit)
                )
            ),
            total = total
        )
}
