package dev.goncalomartins.movies.dto.hypermedia

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
open class Dto<T>(
    @get:JsonProperty(value = "_embedded")
    val embedded: Map<String, List<T>>? = null,

    val total: Long? = null,

    @get:JsonProperty(value = "_links")
    val links: Map<String, Link>? = null
)
