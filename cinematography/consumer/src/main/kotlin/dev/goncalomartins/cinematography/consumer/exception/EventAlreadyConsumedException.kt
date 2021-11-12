package dev.goncalomartins.cinematography.consumer.exception

class EventAlreadyConsumedException(id: String) : RuntimeException("Event with id '$id' was already consumed")
