plugins {
    id("io.quarkus")
    kotlin("jvm")
    kotlin("plugin.allopen")
}

dependencies {
    implementation(project(":common"))
    implementation("io.quarkus:quarkus-smallrye-reactive-messaging-kafka")
}

allOpen {
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}
