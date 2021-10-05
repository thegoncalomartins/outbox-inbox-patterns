package dev.goncalomartins.movies.exception

class MovieNotFoundException(id: String) : NoSuchElementException("Movie with id '$id' does not exist")
