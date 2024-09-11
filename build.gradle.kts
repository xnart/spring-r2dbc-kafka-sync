plugins {
    java
    `maven-publish`
    id("org.springframework.boot") version "3.3.3"
    id("io.spring.dependency-management") version "1.1.6"
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
}

group = "com.github.xnart"

repositories {
    mavenCentral()
}


dependencies {
    compileOnly("org.springframework.boot:spring-boot-starter")
    compileOnly("org.springframework.boot:spring-boot-starter-data-r2dbc")
    compileOnly("org.postgresql:r2dbc-postgresql")
    compileOnly("org.postgresql:postgresql")
    compileOnly("org.springframework.kafka:spring-kafka")

    compileOnly("io.projectreactor.kotlin:reactor-kotlin-extensions")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
    compileOnly("org.jetbrains.kotlin:kotlin-reflect")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])
            artifact(sourcesJar)
        }
    }

    repositories {
        mavenLocal()
    }
}


tasks {
    test {
        useJUnitPlatform()
        failFast = true
    }

    jar {
        enabled = true
    }

    publish {
        dependsOn(check)
    }

    bootJar {
        enabled = false
    }
}
