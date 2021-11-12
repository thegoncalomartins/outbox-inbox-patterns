package dev.goncalomartins.cinematography.consumer.handler

import dev.goncalomartins.cinematography.common.model.inbox.InboxEvent
import io.smallrye.mutiny.Uni

interface Handler {
    fun handle(inboxEvent: InboxEvent): Uni<Void>
}
