package dev.goncalomartins.cinematography.common.exception

class PersonNotFoundException(id: String) : ResourceNotFoundException("Person with id '$id' does not exist")
