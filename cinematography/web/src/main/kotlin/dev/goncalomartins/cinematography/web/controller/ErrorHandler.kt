package dev.goncalomartins.cinematography.web.controller

import dev.goncalomartins.cinematography.common.exception.ResourceNotFoundException
import dev.goncalomartins.cinematography.web.dto.error.ErrorDto
import io.smallrye.mutiny.Uni
import org.jboss.resteasy.reactive.RestResponse
import org.jboss.resteasy.reactive.server.ServerExceptionMapper
import org.slf4j.LoggerFactory
import javax.enterprise.context.ApplicationScoped
import javax.ws.rs.core.Response

@ApplicationScoped
class ErrorHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ServerExceptionMapper
    fun mapException(exception: ResourceNotFoundException): Uni<RestResponse<ErrorDto>> =
        Uni.createFrom().item(
            RestResponse.status(
                Response.Status.NOT_FOUND,
                ErrorDto(Response.Status.NOT_FOUND.statusCode.toString(), exception.message!!)
            )
        ).invoke { _ -> logger.debug(exception.message, exception) }

    @ServerExceptionMapper
    fun mapException(exception: Exception): Uni<RestResponse<ErrorDto>> =
        Uni.createFrom().item(
            RestResponse.status(
                Response.Status.INTERNAL_SERVER_ERROR,
                ErrorDto(
                    Response.Status.INTERNAL_SERVER_ERROR.statusCode.toString(),
                    exception.message ?: "Unexpected error"
                )
            )
        ).invoke { _ -> logger.error(exception.message, exception) }
}
