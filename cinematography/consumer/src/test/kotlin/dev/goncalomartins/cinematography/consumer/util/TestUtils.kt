package dev.goncalomartins.cinematography.consumer.util

import dev.goncalomartins.cinematography.consumer.PeopleTests
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.subscription.MultiEmitter
import io.smallrye.reactive.messaging.kafka.KafkaRecord
import org.eclipse.microprofile.reactive.messaging.Outgoing
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors
import javax.annotation.PreDestroy
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class TestUtils {

    companion object {
        const val PEOPLE_CHANNEL = "people-outgoing"
        const val MOVIES_CHANNEL = "movies-outgoing"
    }

    private var outgoingPeopleStream: Multi<KafkaRecord<String, String>>
    private lateinit var peopleEmitter: MultiEmitter<in KafkaRecord<String, String>>

    private var outgoingMoviesStream: Multi<KafkaRecord<String, String>>
    private lateinit var moviesEmitter: MultiEmitter<in KafkaRecord<String, String>>

    init {
        outgoingPeopleStream = Multi.createFrom().emitter { peopleEmitter = it }
        outgoingMoviesStream = Multi.createFrom().emitter { moviesEmitter = it }
    }

    @PreDestroy
    fun destroy() {
        peopleEmitter.complete()
    }

    @Outgoing(PEOPLE_CHANNEL)
    fun producePeople() = outgoingPeopleStream

    @Outgoing(MOVIES_CHANNEL)
    fun produceMovies() = outgoingMoviesStream

    fun producePeopleMessage(record: KafkaRecord<String, String>) = peopleEmitter.emit(record)

    fun produceMoviesMessage(record: KafkaRecord<String, String>) = moviesEmitter.emit(record)

    fun readFileAsString(file: String): String {
        val resource: InputStream = PeopleTests::class.java.classLoader.getResourceAsStream(file)!!

        return BufferedReader(InputStreamReader(resource, StandardCharsets.UTF_8))
            .lines()
            .collect(Collectors.joining("\n"))
    }
}
