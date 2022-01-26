package dev.goncalomartins.knowledgebase.web.dto.person

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dev.goncalomartins.knowledge-base.common.model.person.Person
import dev.goncalomartins.knowledgebase.web.dto.hypermedia.Dto
import dev.goncalomartins.knowledgebase.web.dto.hypermedia.Link
import java.time.Instant
import java.time.LocalDate

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
class PersonDto(
    val id: String? = null,
    val name: String,
    val birthDate: LocalDate? = LocalDate.now(),
    val createdAt: Instant?,
    val updatedAt: Instant?,
    links: Map<String, Link>? = null
) : Dto<PersonDto>(links = links)

fun Person.toDto(links: Map<String, Link>) = PersonDto(
    id = id,
    name = name,
    birthDate = birthDate,
    links = links,
    createdAt = createdAt,
    updatedAt = updatedAt
)
