package dev.goncalomartins.cinematography.web.filter

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
        val address = request.remoteAddress().toString()

        logger.info("Request {} {} from IP {}", method, path, address)

        context.next()
    }
}
