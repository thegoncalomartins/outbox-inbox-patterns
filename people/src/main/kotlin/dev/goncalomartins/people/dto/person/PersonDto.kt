package dev.goncalomartins.people.dto.person

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import dev.goncalomartins.people.dto.hypermedia.Dto
import dev.goncalomartins.people.dto.hypermedia.Link
import dev.goncalomartins.people.model.person.Person
import org.bson.types.ObjectId
import java.time.Instant
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
class PersonDto(
    val id: String? = null,
    val name: String,
    val birthDate: LocalDate? = LocalDate.now(),
    val createdAt: Instant?,
    val updatedAt: Instant?,
    links: Map<String, Link>? = null
) : Dto<PersonDto>(links = links)

fun PersonDto.toModel(id: String? = null) = Person(
    id = id?.let { ObjectId(it) },
    name = name,
    birthDate = birthDate ?: LocalDate.now()
)

fun Person.toDto(links: Map<String, Link>) = PersonDto(
    id = id?.toHexString(),
    name = name,
    birthDate = birthDate,
    links = links,
    createdAt = createdAt,
    updatedAt = updatedAt
)
