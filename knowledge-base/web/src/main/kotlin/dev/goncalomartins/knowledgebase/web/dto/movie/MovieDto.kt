package dev.goncalomartins.knowledgebase.web.dto.movie

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dev.goncalomartins.knowledge-base.common.model.movie.Movie
import dev.goncalomartins.knowledgebase.web.dto.hypermedia.Dto
import dev.goncalomartins.knowledgebase.web.dto.hypermedia.Link
import java.time.Instant

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
class MovieDto(
    val id: String? = null,
    val title: String,
    val released: Int,
    val createdAt: Instant?,
    val updatedAt: Instant?,
    links: Map<String, Link>? = null
) : Dto<MovieDto>(links = links)

fun Movie.toDto(links: Map<String, Link>) = MovieDto(
    id = id,
    title = title,
    released = released,
    links = links,
    createdAt = createdAt,
    updatedAt = updatedAt
)
