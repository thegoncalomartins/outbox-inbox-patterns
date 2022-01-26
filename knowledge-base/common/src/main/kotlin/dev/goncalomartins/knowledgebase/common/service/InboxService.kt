package dev.goncalomartins.knowledgebase.common.service

import dev.goncalomartins.knowledge-base.common.model.inbox.InboxEvent
import dev.goncalomartins.knowledge-base.common.repository.InboxRepository
import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.opentracing.Traced
import org.neo4j.driver.reactive.RxTransaction
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
@Traced
class InboxService(val inboxRepository: InboxRepository) {
    fun save(transaction: RxTransaction, inboxEvent: InboxEvent): Uni<Void> =
        inboxRepository.save(transaction, inboxEvent)

    fun findOne(transaction: RxTransaction, id: String): Uni<InboxEvent?> = inboxRepository.findOne(transaction, id)
}
