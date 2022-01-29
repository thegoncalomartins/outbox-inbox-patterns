package dev.goncalomartins.knowledgebase.consumer.handler

import dev.goncalomartins.knowledgebase.common.model.inbox.InboxEvent
import io.smallrye.mutiny.Uni

interface Handler {
    fun handle(inboxEvent: InboxEvent): Uni<Void>
}
