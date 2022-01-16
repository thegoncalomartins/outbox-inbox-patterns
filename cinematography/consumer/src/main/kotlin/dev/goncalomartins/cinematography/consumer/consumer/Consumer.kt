package dev.goncalomartins.cinematography.consumer.consumer

import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.reactive.messaging.Message

interface Consumer<T> {
    fun consume(message: Message<T>): Uni<Void>
}
