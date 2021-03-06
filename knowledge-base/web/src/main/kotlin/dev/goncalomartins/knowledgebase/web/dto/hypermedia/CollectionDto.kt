package dev.goncalomartins.knowledgebase.web.dto.hypermedia

open class CollectionDto<T>(
    embedded: Map<String, T>,
    total: Long,
    links: Map<String, Link>? = null
) : Dto<T>(total = total, links = links, embedded = embedded)
