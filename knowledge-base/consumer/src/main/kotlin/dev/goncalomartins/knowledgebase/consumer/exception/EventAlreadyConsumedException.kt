package dev.goncalomartins.knowledgebase.consumer.exception

class EventAlreadyConsumedException(id: String) : RuntimeException("Event with id '$id' was already consumed")
