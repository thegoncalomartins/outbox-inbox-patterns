package dev.goncalomartins.movies.dto.movie

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dev.goncalomartins.movies.dto.hypermedia.Dto
import dev.goncalomartins.movies.dto.hypermedia.Link
import dev.goncalomartins.movies.model.movie.Movie
import org.bson.types.ObjectId
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
class MovieDto(
    val id: String? = null,
    val name: String,
    val year: Int,
    val directedBy: Set<String>? = emptySet(),
    val cast: Set<String>? = emptySet(),
    val createdAt: Instant?,
    val updatedAt: Instant?,
    links: Map<String, Link>? = null
) : Dto<MovieDto>(links = links)

fun MovieDto.toModel(id: String? = null) = Movie(
    id = id?.let { ObjectId(it) },
    name = name,
    year = year,
    directedBy = directedBy ?: emptySet(),
    cast = cast ?: emptySet()
)

fun Movie.toDto(links: Map<String, Link>) = MovieDto(
    id = id?.toHexString(),
    name = name,
    year = year,
    directedBy = directedBy,
    cast = cast,
    links = links,
    createdAt = createdAt,
    updatedAt = updatedAt
)
