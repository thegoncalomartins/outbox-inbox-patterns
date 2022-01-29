plugins {
    id("io.quarkus")
    kotlin("jvm")
    kotlin("plugin.allopen")
}

val vertxCoreVersion: String by extra("4.1.3")
val jacksonDatabindVersion: String by extra("2.12.4")
val mockitoKotlinVersion: String by extra("4.0.0")
val mockitoJunitJupiterVersion: String by extra("4.3.1")

dependencies {
    api("io.quarkus:quarkus-neo4j")
    implementation("io.vertx:vertx-core:$vertxCoreVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoJunitJupiterVersion")
}

allOpen {
    annotation("javax.enterprise.context.ApplicationScoped")
}
