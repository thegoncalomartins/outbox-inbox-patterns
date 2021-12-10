package dev.goncalomartins.cinematography.common.util

import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.opentracing.Traced
import org.neo4j.driver.Driver
import org.neo4j.driver.reactive.RxTransaction
import java.util.function.Supplier
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
@Traced
class DatabaseUtils(val driver: Driver) {

    fun <T> inTransaction(uni: (tx: RxTransaction) -> Uni<T>): Uni<T> =
        Uni
            .createFrom()
            .item { driver.rxSession() }
            .flatMap { session ->
                Uni
                    .createFrom()
                    .publisher(session.beginTransaction())
                    .flatMap { transaction ->
                        uni.invoke(transaction)
                            .onItem()
                            .call(Supplier { Uni.createFrom().publisher<Void>(transaction.commit()) })
                            .onFailure()
                            .call(Supplier { Uni.createFrom().publisher<Void>(transaction.rollback()) })
                    }.onTermination()
                    .call(Supplier { Uni.createFrom().publisher<Void>(session.close()) })
            }
}
