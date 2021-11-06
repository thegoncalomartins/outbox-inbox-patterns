package dev.goncalomartins.movies.controller

import dev.goncalomartins.movies.controller.MovieController.Companion.PATH
import dev.goncalomartins.movies.dto.error.ErrorDto
import dev.goncalomartins.movies.dto.hypermedia.CollectionDto
import dev.goncalomartins.movies.dto.movie.MovieDto
import dev.goncalomartins.movies.dto.movie.toDto
import dev.goncalomartins.movies.exception.MovieNotFoundException
import dev.goncalomartins.movies.model.movie.Movie
import dev.goncalomartins.movies.service.MovieService
import dev.goncalomartins.movies.service.MovieService.Companion.DEFAULT_LIMIT
import dev.goncalomartins.movies.service.MovieService.Companion.DEFAULT_SKIP
import dev.goncalomartins.movies.util.ControllerUtils
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
class MovieController(
    var movieService: MovieService,
    var controllerUtils: ControllerUtils
) {

    companion object {
        const val PATH = "/api/movies"
        const val PATH_FOR_ONE = "/api/movies/{id}"
        const val LIMIT_PARAMETER = "limit"
        const val SKIP_PARAMETER = "skip"
        const val ID_PARAMETER = "id"
    }

    @GET
    fun findAll(
        @QueryParam(LIMIT_PARAMETER) @DefaultValue(DEFAULT_LIMIT.toString()) limit: Int,
        @QueryParam(SKIP_PARAMETER) @DefaultValue(DEFAULT_SKIP.toString()) skip: Int
    ): Uni<RestResponse<CollectionDto<MovieDto>>> =
        movieService.findAll()
            .map { RestResponse.ok(toDto(total = it.total, limit = limit, skip = skip, movies = it.movies)) }

    @GET
    @Path("/{id}")
    fun findOne(@PathParam(ID_PARAMETER) id: String): Uni<RestResponse<MovieDto>> =
        movieService.findOne(id).map { RestResponse.ok(toDto(it)) }

    @POST
    @Blocking
    fun create(movie: MovieDto): Uni<RestResponse<MovieDto>> =
        movieService.create(movie.toMovie())
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
    fun update(@PathParam(ID_PARAMETER) id: String, movie: MovieDto): Uni<RestResponse<MovieDto>> =
        movieService.update(movie.toMovie(id)).map { RestResponse.ok(toDto(it)) }

    @DELETE
    @Path("/{id}")
    @Blocking
    fun delete(@PathParam(ID_PARAMETER) id: String): Uni<RestResponse<Void>> = movieService.delete(id)
        .map { RestResponse.noContent() }

    @ServerExceptionMapper
    fun mapException(exception: MovieNotFoundException): Uni<RestResponse<ErrorDto>> =
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

    private fun toDto(movie: Movie) = movie.toDto(
        links = mapOf(
            "self" to controllerUtils.buildLink(
                path = PATH_FOR_ONE,
                uriVariables = mapOf("id" to movie.id!!.toHexString())
            )
        )
    )

    private fun toDto(total: Long, limit: Int = DEFAULT_LIMIT, skip: Int = DEFAULT_SKIP, movies: List<Movie>) =
        CollectionDto(
            embedded = mapOf("movies" to movies.map { toDto(it) }),
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
