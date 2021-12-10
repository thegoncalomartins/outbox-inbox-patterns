plugins {
    id("io.quarkus")
    kotlin("jvm")
    kotlin("plugin.allopen")
}

val vertxCoreVersion: String by extra("4.1.3")
val jacksonDatabindVersion: String by extra("2.12.4")

dependencies {
    api("io.quarkus:quarkus-neo4j")
    implementation("io.vertx:vertx-core:$vertxCoreVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")
}

allOpen {
    annotation("javax.enterprise.context.ApplicationScoped")
}
