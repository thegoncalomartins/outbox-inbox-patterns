package dev.goncalomartins.movies.dto.hypermedia

open class CollectionDto<T>(
    embedded: Map<String, List<T>>,
    total: Long,
    links: Map<String, Link>? = null
) : Dto<T>(total = total, links = links, embedded = embedded)
