package dev.goncalomartins.people.controller

import dev.goncalomartins.people.controller.PersonController.Companion.PATH
import dev.goncalomartins.people.dto.error.ErrorDto
import dev.goncalomartins.people.dto.hypermedia.CollectionDto
import dev.goncalomartins.people.dto.person.PersonDto
import dev.goncalomartins.people.dto.person.toDto
import dev.goncalomartins.people.dto.person.toModel
import dev.goncalomartins.people.exception.PersonNotFoundException
import dev.goncalomartins.people.model.person.Person
import dev.goncalomartins.people.service.PersonService
import dev.goncalomartins.people.service.PersonService.Companion.DEFAULT_LIMIT
import dev.goncalomartins.people.service.PersonService.Companion.DEFAULT_SKIP
import dev.goncalomartins.people.util.ControllerUtils
import io.smallrye.common.annotation.Blocking
import io.smallrye.mutiny.Uni
import org.jboss.resteasy.reactive.RestResponse
import org.jboss.resteasy.reactive.server.ServerExceptionMapper
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path(PATH)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class PersonController(
    var movieService: PersonService,
    var controllerUtils: ControllerUtils
) {

    companion object {
        const val PATH = "/api/people"
        const val PATH_FOR_ONE = "/api/people/{id}"
        const val LIMIT_PARAMETER = "limit"
        const val SKIP_PARAMETER = "skip"
        const val ID_PARAMETER = "id"
    }

    @GET
    fun findAll(
        @QueryParam(LIMIT_PARAMETER) @DefaultValue(DEFAULT_LIMIT.toString()) limit: Int,
        @QueryParam(SKIP_PARAMETER) @DefaultValue(DEFAULT_SKIP.toString()) skip: Int
    ): Uni<RestResponse<CollectionDto<PersonDto>>> =
        movieService.findAll()
            .map { RestResponse.ok(toDto(total = it.total, limit = limit, skip = skip, people = it.people)) }

    @GET
    @Path("/{id}")
    fun findOne(@PathParam(ID_PARAMETER) id: String): Uni<RestResponse<PersonDto>> =
        movieService.findOne(id).map { RestResponse.ok(toDto(it)) }

    @POST
    @Blocking
    fun create(movie: PersonDto): Uni<RestResponse<PersonDto>> =
        movieService.create(movie.toModel())
            .map {
                RestResponse.ResponseBuilder.create(Response.Status.CREATED, toDto(it)).location(
                    controllerUtils.buildLink(
                        path = PATH_FOR_ONE,
                        uriVariables = mapOf("id" to it.id!!.toHexString())
                    ).href
                ).build()
            }

    @PUT
    @Path("/{id}")
    @Blocking
    fun update(@PathParam(ID_PARAMETER) id: String, movie: PersonDto): Uni<RestResponse<PersonDto>> =
        movieService.update(movie.toModel(id)).map { RestResponse.ok(toDto(it)) }

    @DELETE
    @Path("/{id}")
    @Blocking
    fun delete(@PathParam(ID_PARAMETER) id: String): Uni<RestResponse<Void>> = movieService.delete(id)
        .map { RestResponse.noContent() }

    @ServerExceptionMapper
    fun mapException(exception: PersonNotFoundException): Uni<RestResponse<ErrorDto>> =
        Uni.createFrom().item(
            RestResponse.status(
                Response.Status.NOT_FOUND,
                ErrorDto(Response.Status.NOT_FOUND.statusCode.toString(), exception.message!!)
            )
        )

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
        )

    private fun toDto(movie: Person) = movie.toDto(
        links = mapOf(
            "self" to controllerUtils.buildLink(
                path = PATH_FOR_ONE,
                uriVariables = mapOf("id" to movie.id!!.toHexString())
            )
        )
    )

    private fun toDto(total: Long, limit: Int = DEFAULT_LIMIT, skip: Int = DEFAULT_SKIP, people: List<Person>) =
        CollectionDto(
            embedded = mapOf("people" to people.map { toDto(it) }),
            links = mapOf(
                "first" to controllerUtils.buildLink(
                    path = PATH,
                    queryParams = controllerUtils.calculateFirst(limit)
                ),
                "previous" to controllerUtils.buildLink(
                    path = PATH,
                    queryParams = controllerUtils.calculatePrevious(limit = limit, skip = skip)
                ),
                "self" to controllerUtils.buildLink(
                    path = PATH,
                    queryParams = mapOf("limit" to limit.toString(), "skip" to skip.toString())
                ),
                "next" to controllerUtils.buildLink(
                    path = PATH,
                    queryParams = controllerUtils.calculateNext(total = total, limit = limit, skip = skip)
                ),
                "last" to controllerUtils.buildLink(
                    path = PATH,
                    queryParams = controllerUtils.calculateLast(total = total, limit = limit)
                )
            ),
            total = total
        )
}
