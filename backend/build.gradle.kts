val slf4jVersion = "2.0.16" // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
val flywayVersion = "10.22.0" // https://plugins.gradle.org/plugin/org.flywaydb.flyway
val mariadbJavaClientVersion = "3.5.1" // https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client
val jooqVersion = "3.19.18" // https://mvnrepository.com/artifact/org.jooq/jooq

plugins {
    java
    idea
    application
    alias(libs.plugins.kotlin.jvm)
    id("org.flywaydb.flyway") version "10.22.0" // https://plugins.gradle.org/plugin/org.flywaydb.flyway
    id("nu.studer.jooq") version "9.0" // https://plugins.gradle.org/plugin/nu.studer.jooq
    id("com.github.johnrengelman.shadow") version "8.1.1" // https://plugins.gradle.org/plugin/com.github.johnrengelman.shadow
}

idea {
    module.excludeDirs.add(file("out"))
}

sourceSets {
    main {
        java.srcDirs("src")
        resources.srcDirs("res")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    jvmTargetValidationMode.set(org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode.ERROR)
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.reflections:reflections:0.10.2")

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.default.headers)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.gson)

    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-log4j12:$slf4jVersion")

    implementation("com.zaxxer:HikariCP:6.2.1") // https://mvnrepository.com/artifact/com.zaxxer/HikariCP
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    runtimeOnly("org.flywaydb:flyway-mysql:$flywayVersion")

    runtimeOnly("org.mariadb.jdbc:mariadb-java-client:$mariadbJavaClientVersion")
    jooqGenerator("org.mariadb.jdbc:mariadb-java-client:$mariadbJavaClientVersion")
}

tasks.register<JavaExec>("mapgen") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.origin.generator.MapGenerator")
}

tasks.register<JavaExec>("mapimport") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.origin.MapImporter")
}

application {
    mainClass.set("com.origin.ServerLauncher")
}

tasks {
    jar { enabled = false }
    distZip { enabled = false }
    distTar { enabled = false }
    startScripts { enabled = false }
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveClassifier.set("") // Optional, if you want to specify the output classifier

        // Configures the task to merge service files in META-INF/services
        mergeServiceFiles()

        manifest {
            attributes(
                "Implementation-Title" to "origin",
                "Implementation-Vendor" to "arksu"
            )
        }
    }
}

buildscript {
    dependencies {
        classpath("org.flywaydb:flyway-mysql:10.22.0")
        classpath("org.mariadb.jdbc:mariadb-java-client:3.5.1")
    }
}

val dbHost = System.getenv("DB_HOST") ?: "localhost"

flyway {
    url = "jdbc:mariadb://${dbHost}:3406/origin"
    user = "origin"
    password = "origin"
    schemas = arrayOf("origin")
    cleanDisabled = false
    locations = arrayOf("classpath:db/migration")
}

jooq {
    version.set(jooqVersion)

    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(true)

            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = "org.mariadb.jdbc.Driver"
                    url = "jdbc:mariadb://${dbHost}:3406/origin"
                    user = "origin"
                    password = "origin"
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.mariadb.MariaDBDatabase"
                        inputSchema = "origin"
                        excludes = "flyway_schema_history"
                    }
                    generate.apply {
                        isPojos = true
                        isImmutablePojos = true
                        isPojosAsKotlinDataClasses = true
                        isKotlinNotNullPojoAttributes = true
                        isKotlinNotNullRecordAttributes = true
                        isDeprecated = false
                        isRecords = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "com.origin.jooq"
                        directory = "build/generated/jooq/src"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}