package dev.goncalomartins.movies.dto.movie

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dev.goncalomartins.movies.dto.hypermedia.Dto
import dev.goncalomartins.movies.dto.hypermedia.Link
import dev.goncalomartins.movies.model.movie.Movie
import dev.goncalomartins.movies.model.movie.Role
import org.bson.types.ObjectId
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
class MovieDto(
    val id: String? = null,
    val title: String,
    val released: Int,
    val directedBy: Set<String>? = emptySet(),
    val cast: Set<RoleDto>? = emptySet(),
    val createdAt: Instant?,
    val updatedAt: Instant?,
    links: Map<String, Link>? = null
) : Dto<MovieDto>(links = links) {
    fun toMovie(id: String? = null) = Movie(
        id = id?.let { ObjectId(it) },
        title = title,
        released = released,
        directedBy = directedBy ?: emptySet(),
        cast = cast?.map(RoleDto::toRole)?.toSet() ?: emptySet()
    )
}

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class RoleDto(val personId: String?, val role: String?) {
    fun toRole() = Role(personId!!, role!!)
}

fun Role.toDto() = RoleDto(personId = personId, role = role)

fun Movie.toDto(links: Map<String, Link>) = MovieDto(
    id = id?.toHexString(),
    title = title,
    released = released,
    directedBy = directedBy,
    cast = cast.map(Role::toDto).toSet(),
    links = links,
    createdAt = createdAt,
    updatedAt = updatedAt
)
