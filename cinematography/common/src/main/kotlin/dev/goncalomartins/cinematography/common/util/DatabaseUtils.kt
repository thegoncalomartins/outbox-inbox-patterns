package dev.goncalomartins.cinematography.common.util

import io.smallrye.mutiny.Uni
import org.neo4j.driver.Driver
import org.neo4j.driver.reactive.RxTransaction
import java.util.function.Supplier
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class DatabaseUtils(val driver: Driver) {

    fun inTransaction(block: (tx: RxTransaction) -> Uni<Void>): Uni<Void> =
        Uni
            .createFrom()
            .item { driver.rxSession() }
            .flatMap { session ->
                Uni
                    .createFrom()
                    .publisher(session.beginTransaction())
                    .flatMap { transaction ->
                        block.invoke(transaction)
                            .onItem()
                            .call(Supplier { Uni.createFrom().publisher<Void>(transaction.commit()) })
                            .onFailure()
                            .call(Supplier { Uni.createFrom().publisher<Void>(transaction.rollback()) })
                    }.onTermination()
                    .call(Supplier { Uni.createFrom().publisher<Void>(session.close()) })
            }
}
