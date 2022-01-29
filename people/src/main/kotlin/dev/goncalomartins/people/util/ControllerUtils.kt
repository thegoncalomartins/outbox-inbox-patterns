package dev.goncalomartins.people.util

import dev.goncalomartins.people.dto.hypermedia.Link
import io.vertx.ext.web.RoutingContext
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.net.InetAddress
import java.net.URI
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.core.UriBuilder

@ApplicationScoped
class ControllerUtils(
    @ConfigProperty(name = "quarkus.http.port") val port: Int
) {
    /**
     * Build hypermedia link
     *
     * @param context the routing context
     * @param path the endpoint route
     * @param queryParams map containing the query parameters
     * @param uriVariables map containing the URI variables.
     * @return a [Link] instance representing the hypermedia link that was built
     */
    fun buildLink(
        context: RoutingContext,
        path: String,
        queryParams: Map<String, String> = mapOf(),
        uriVariables: Map<String, String> = mapOf()
    ): Link = Link(
        UriBuilder.fromUri(host(context))
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

    private fun host(context: RoutingContext): URI {
        val apiGatewayHost = context
            .request()
            .getHeader("X-Forwarded-For")
            ?.split(",")
            ?.lastOrNull()

        val ownHost = "${InetAddress.getLocalHost().hostAddress}:$port"
        val host = apiGatewayHost ?: ownHost

        val forwardedProtocol = context
            .request()
            .getHeader("X-Forwarded-Proto")

        val protocol = forwardedProtocol ?: "http"

        return URI.create("$protocol://$host")
    }
}
