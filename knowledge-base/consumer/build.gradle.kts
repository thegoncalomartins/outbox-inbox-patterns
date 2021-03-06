val micrometerRegistryPrometheusVersion by extra("1.8.1")
val awaitilityKotlinVersion by extra("4.1.1")

plugins {
    id("io.quarkus")
    kotlin("jvm")
    kotlin("plugin.allopen")
}

dependencies {
    implementation(project(":common"))
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerRegistryPrometheusVersion")
    implementation("io.quarkus:quarkus-rest-client")
    implementation("io.quarkus:quarkus-smallrye-reactive-messaging-kafka")
    testImplementation("org.awaitility:awaitility-kotlin:$awaitilityKotlinVersion")
}

allOpen {
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}
