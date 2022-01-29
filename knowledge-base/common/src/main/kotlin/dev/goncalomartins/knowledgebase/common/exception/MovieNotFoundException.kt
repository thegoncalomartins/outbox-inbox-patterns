package dev.goncalomartins.knowledgebase.common.exception

class MovieNotFoundException(id: String) : ResourceNotFoundException("Movie with id '$id' does not exist")
