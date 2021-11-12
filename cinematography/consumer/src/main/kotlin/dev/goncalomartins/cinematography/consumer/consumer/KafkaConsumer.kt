package dev.goncalomartins.cinematography.consumer.consumer

import io.smallrye.mutiny.Uni
import io.smallrye.reactive.messaging.kafka.IncomingKafkaRecord

interface KafkaConsumer<K, T> {
    fun consume(record: IncomingKafkaRecord<K, T>): Uni<Void>
}
