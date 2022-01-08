package dev.goncalomartins.cinematography.consumer.job

import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.quarkus.runtime.StartupEvent
import io.smallrye.mutiny.Multi
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Duration
import java.util.concurrent.Executors
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Observes
import javax.ws.rs.client.ClientBuilder
import javax.ws.rs.client.Entity
import javax.ws.rs.core.MediaType

@ApplicationScoped
class PushMetricsJob(
    @ConfigProperty(name = "intervals.push-metrics")
    val pushMetricsInterval: Duration,
    @ConfigProperty(name = "pushgateway.uri")
    val pushgatewayURI: URI
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val prometheusRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    private val webClient = ClientBuilder
        .newBuilder()
        .build()

    init {
        ClassLoaderMetrics().bindTo(prometheusRegistry)
        JvmMemoryMetrics().bindTo(prometheusRegistry)
        JvmGcMetrics().bindTo(prometheusRegistry)
        ProcessorMetrics().bindTo(prometheusRegistry)
        JvmThreadMetrics().bindTo(prometheusRegistry)
    }

    fun pushMetrics(@Observes ev: StartupEvent) {
        Multi
            .createFrom()
            .ticks()
            .every(pushMetricsInterval)
            .invoke(Runnable { logger.info("Pushing metrics...") })
            .map {
                val body = prometheusRegistry.scrape()
                val target = webClient.target(pushgatewayURI)
                target
                    .request()
                    .post(Entity.entity(body, MediaType.APPLICATION_OCTET_STREAM_TYPE))
            }
            .runSubscriptionOn(Executors.newSingleThreadExecutor())
            .subscribe()
            .with { logger.info("Successfully pushed metrics") }
    }
}
