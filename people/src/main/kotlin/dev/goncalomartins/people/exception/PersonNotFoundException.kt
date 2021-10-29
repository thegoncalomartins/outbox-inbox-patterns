package dev.goncalomartins.people.exception

class PersonNotFoundException(id: String) : NoSuchElementException("Person with id '$id' does not exist")
