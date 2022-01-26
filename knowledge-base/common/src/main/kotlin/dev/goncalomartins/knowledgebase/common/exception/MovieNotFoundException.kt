package dev.goncalomartins.knowledgebase.common.exception

class MovieNotFoundException(id: String) : dev.goncalomartins.knowledgebase.common.exception.ResourceNotFoundException("Movie with id '$id' does not exist")
