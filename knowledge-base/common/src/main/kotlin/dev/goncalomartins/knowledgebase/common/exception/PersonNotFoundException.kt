package dev.goncalomartins.knowledgebase.common.exception

class PersonNotFoundException(id: String) : dev.goncalomartins.knowledgebase.common.exception.ResourceNotFoundException("Person with id '$id' does not exist")
