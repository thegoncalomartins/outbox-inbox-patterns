import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.21" apply false
    kotlin("plugin.allopen") version "1.5.21" apply false
    id("io.quarkus") apply false
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

allprojects {
    group = "dev.goncalomartins"
    version = "1.0.0-SNAPSHOT"

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.VERSION_11.toString()
        targetCompatibility = JavaVersion.VERSION_11.toString()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
        kotlinOptions.javaParameters = true
    }
}

subprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }

    apply {
        plugin("java")
    }

    val implementation by configurations
    val testImplementation by configurations

    dependencies {
        implementation(enforcedPlatform("$quarkusPlatformGroupId:$quarkusPlatformArtifactId:$quarkusPlatformVersion"))
        implementation("io.quarkus:quarkus-smallrye-opentracing")
        implementation("io.quarkus:quarkus-kotlin")
        implementation("io.quarkus:quarkus-arc")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
        testImplementation("io.quarkus:quarkus-junit5")
    }
}
