package dev.goncalomartins.knowledgebase.web.controller

import dev.goncalomartins.knowledgebase.common.exception.MovieNotFoundException
import dev.goncalomartins.knowledgebase.common.model.movie.Movie
import dev.goncalomartins.knowledgebase.common.service.MovieService
import dev.goncalomartins.knowledgebase.common.service.MovieService.Companion.DEFAULT_LIMIT
import dev.goncalomartins.knowledgebase.common.service.MovieService.Companion.DEFAULT_SKIP
import dev.goncalomartins.knowledgebase.web.controller.MovieController.Companion.PATH
import dev.goncalomartins.knowledgebase.web.dto.graph.GraphDto
import dev.goncalomartins.knowledgebase.web.dto.hypermedia.CollectionDto
import dev.goncalomartins.knowledgebase.web.dto.movie.MovieDto
import dev.goncalomartins.knowledgebase.web.dto.movie.toDto
import dev.goncalomartins.knowledgebase.web.util.ControllerUtils
import io.micrometer.core.annotation.Counted
import io.micrometer.core.annotation.Timed
import io.smallrye.mutiny.Uni
import io.vertx.ext.web.RoutingContext
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
class MovieController(
    val movieService: MovieService,
    val controllerUtils: ControllerUtils
) {
    companion object {
        const val PATH = "/api/knowledge-base/movies"
        const val PATH_FOR_ONE = "/api/knowledge-base/movies/{id}"
        const val LIMIT_PARAMETER = "limit"
        const val SKIP_PARAMETER = "skip"
        const val ID_PARAMETER = "id"
    }

    @Counted(
        value = "api.knowledgebase.movies.findAll.count",
        description = "How many times GET /api/knowledge-base/movies has been requested"
    )
    @Timed(
        value = "api.knowledgebase.movies.findAll.time",
        description = "Response time for GET /api/knowledge-base/movies"
    )
    @GET
    fun findAll(
        @QueryParam(LIMIT_PARAMETER) @DefaultValue(DEFAULT_LIMIT.toString()) limit: Int,
        @QueryParam(SKIP_PARAMETER) @DefaultValue(DEFAULT_SKIP.toString()) skip: Int,
        context: RoutingContext
    ): Uni<RestResponse<CollectionDto<List<MovieDto>>>> =
        movieService.findAll(skip, limit)
            .map {
                RestResponse.ok(
                    toDto(
                        total = it.total,
                        limit = limit,
                        skip = skip,
                        movies = it.movies,
                        context = context
                    )
                )
            }

    @Counted(
        value = "api.knowledgebase.movies.findOne.count",
        description = "How many times GET /api/knowledge-base/movies/{id} has been requested"
    )
    @Timed(
        value = "api.knowledgebase.movies.findOne.time",
        description = "Response time for GET /api/knowledge-base/movies/{id}"
    )
    @GET
    @Path("/{id}")
    fun findOne(
        @PathParam(ID_PARAMETER) id: String,
        @QueryParam(LIMIT_PARAMETER) @DefaultValue(DEFAULT_LIMIT.toString()) limit: Int,
        @QueryParam(SKIP_PARAMETER) @DefaultValue(DEFAULT_SKIP.toString()) skip: Int,
        context: RoutingContext
    ): Uni<RestResponse<CollectionDto<GraphDto>>> =
        movieService.findOne(id, skip, limit)
            .invoke { graph ->
                if (graph.isEmpty()) throw MovieNotFoundException(
                    id
                )
            }
            .map {
                RestResponse.ok(
                    controllerUtils.toDto(
                        context = context,
                        path = PATH_FOR_ONE,
                        id = id,
                        total = it.total(),
                        limit = limit,
                        skip = skip,
                        graph = it
                    )
                )
            }

    private fun toDto(movie: Movie, context: RoutingContext) = movie.toDto(
        links = mapOf(
            "self" to controllerUtils.buildLink(
                context = context,
                path = PATH_FOR_ONE,
                uriVariables = mapOf("id" to movie.id)
            )
        )
    )

    private fun toDto(
        total: Long,
        limit: Int = DEFAULT_LIMIT,
        skip: Int = DEFAULT_SKIP,
        movies: List<Movie>,
        context: RoutingContext
    ) =
        CollectionDto(
            embedded = mapOf("movies" to movies.map { toDto(it, context) }),
            links = mapOf(
                "first" to controllerUtils.buildLink(
                    context = context,
                    path = PATH,
                    queryParams = controllerUtils.calculateFirst(limit)
                ),
                "previous" to controllerUtils.buildLink(
                    context = context,
                    path = PATH,
                    queryParams = controllerUtils.calculatePrevious(limit = limit, skip = skip)
                ),
                "self" to controllerUtils.buildLink(
                    context = context,
                    path = PATH,
                    queryParams = mapOf("limit" to limit.toString(), "skip" to skip.toString())
                ),
                "next" to controllerUtils.buildLink(
                    context = context,
                    path = PATH,
                    queryParams = controllerUtils.calculateNext(total = total, limit = limit, skip = skip)
                ),
                "last" to controllerUtils.buildLink(
                    context = context,
                    path = PATH,
                    queryParams = controllerUtils.calculateLast(total = total, limit = limit)
                )
            ),
            total = total
        )
}
