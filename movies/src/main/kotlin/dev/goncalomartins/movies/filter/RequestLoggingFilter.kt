package dev.goncalomartins.movies.filter

import io.quarkus.vertx.web.RouteFilter
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class RequestLoggingFilter {

    private val logger = LoggerFactory.getLogger(javaClass)

    @RouteFilter
    fun filter(context: RoutingContext) {
        val request = context.request()
        val method = request.method().name()
        val path = URLDecoder.decode(request.uri(), StandardCharsets.UTF_8)
        val xForwardedForAddress = request.getHeader("X-Forwarded-For")?.split(",")?.firstOrNull()
        val address = xForwardedForAddress ?: request.remoteAddress().toString()

        logger.info("Request {} {} from IP {}", method, path, address)

        context.next()
    }
}
