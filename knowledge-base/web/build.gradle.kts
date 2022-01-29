plugins {
    id("io.quarkus")
    kotlin("jvm")
    kotlin("plugin.allopen")
}

val mockitoKotlinVersion: String by extra("4.0.0")
val mockitoJunitJupiterVersion: String by extra("4.3.1")

dependencies {
    implementation(project(":common"))
    implementation("io.quarkus:quarkus-resteasy-reactive-jackson")
    implementation("io.quarkus:quarkus-vertx-web")
    implementation("io.quarkus:quarkus-smallrye-health")
    implementation("io.quarkus:quarkus-micrometer")
    implementation("io.quarkus:quarkus-micrometer-registry-prometheus")
    testImplementation("io.rest-assured:rest-assured")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoJunitJupiterVersion")
}

allOpen {
    annotation("javax.ws.rs.Path")
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}
