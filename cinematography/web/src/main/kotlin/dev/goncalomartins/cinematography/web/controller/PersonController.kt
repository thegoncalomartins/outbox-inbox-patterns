package dev.goncalomartins.cinematography.web.controller

import dev.goncalomartins.cinematography.common.exception.PersonNotFoundException
import dev.goncalomartins.cinematography.common.model.person.Person
import dev.goncalomartins.cinematography.common.service.PersonService
import dev.goncalomartins.cinematography.common.service.PersonService.Companion.DEFAULT_LIMIT
import dev.goncalomartins.cinematography.common.service.PersonService.Companion.DEFAULT_SKIP
import dev.goncalomartins.cinematography.web.controller.PersonController.Companion.PATH
import dev.goncalomartins.cinematography.web.dto.graph.GraphDto
import dev.goncalomartins.cinematography.web.dto.hypermedia.CollectionDto
import dev.goncalomartins.cinematography.web.dto.person.PersonDto
import dev.goncalomartins.cinematography.web.dto.person.toDto
import dev.goncalomartins.cinematography.web.util.ControllerUtils
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.opentracing.Traced
import org.jboss.resteasy.reactive.RestResponse
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType

@Path(PATH)
@Produces(MediaType.APPLICATION_JSON)
@Traced
class PersonController(
    val personService: PersonService,
    val controllerUtils: ControllerUtils
) {
    companion object {
        const val PATH = "/api/cinematography/people"
        const val PATH_FOR_ONE = "/api/cinematography/people/{id}"
        const val LIMIT_PARAMETER = "limit"
        const val SKIP_PARAMETER = "skip"
        const val ID_PARAMETER = "id"
    }

    @Counted(
        value = "api.cinematography.people.findAll.count",
        description = "How many times GET /api/cinematography/people has been requested"
    )
    @Timed(
        value = "api.cinematography.people.findAll.time",
        description = "Response time for GET /api/cinematography/people"
    )
    @GET
    fun findAll(
        @QueryParam(LIMIT_PARAMETER) @DefaultValue(DEFAULT_LIMIT.toString()) limit: Int,
        @QueryParam(SKIP_PARAMETER) @DefaultValue(DEFAULT_SKIP.toString()) skip: Int
    ): Uni<RestResponse<CollectionDto<List<PersonDto>>>> =
        personService.findAll(skip, limit)
            .map { RestResponse.ok(toDto(total = it.total, limit = limit, skip = skip, people = it.people)) }

    @Counted(
        value = "api.cinematography.people.findOne.count",
        description = "How many times GET /api/cinematography/people/{id} has been requested"
    )
    @Timed(
        value = "api.cinematography.people.findOne.time",
        description = "Response time for GET /api/cinematography/people/{id}"
    )
    @GET
    @Path("/{id}")
    fun findOne(
        @PathParam(ID_PARAMETER) id: String,
        @QueryParam(LIMIT_PARAMETER) @DefaultValue(DEFAULT_LIMIT.toString()) limit: Int,
        @QueryParam(SKIP_PARAMETER) @DefaultValue(DEFAULT_SKIP.toString()) skip: Int
    ): Uni<RestResponse<CollectionDto<GraphDto>>> =
        personService.findOne(id, skip, limit)
            .invoke { graph -> if (graph.isEmpty()) throw PersonNotFoundException(id) }
            .map {
                RestResponse.ok(
                    controllerUtils.toDto(
                        PATH_FOR_ONE,
                        id = id,
                        total = it.total(),
                        limit = limit,
                        skip = skip,
                        graph = it
                    )
                )
            }

    private fun toDto(person: Person) = person.toDto(
        links = mapOf(
            "self" to controllerUtils.buildLink(
                path = PATH_FOR_ONE,
                uriVariables = mapOf("id" to person.id)
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
