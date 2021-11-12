plugins {
    id("io.quarkus")
    kotlin("jvm")
    kotlin("plugin.allopen")
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project
val vertxCoreVersion: String by extra("4.1.3")
val jacksonDatabindVersion: String by extra("2.12.4")

dependencies {
    implementation(enforcedPlatform("$quarkusPlatformGroupId:$quarkusPlatformArtifactId:$quarkusPlatformVersion"))
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-arc")
    api("io.quarkus:quarkus-neo4j")
    implementation("io.vertx:vertx-core:$vertxCoreVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonDatabindVersion")
    testImplementation("io.quarkus:quarkus-junit5")
}

allOpen {
    annotation("javax.enterprise.context.ApplicationScoped")
}
