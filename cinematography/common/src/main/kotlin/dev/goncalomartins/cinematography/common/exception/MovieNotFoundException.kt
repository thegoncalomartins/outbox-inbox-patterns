package dev.goncalomartins.cinematography.common.exception

class MovieNotFoundException(id: String) : ResourceNotFoundException("Movie with id '$id' does not exist")
