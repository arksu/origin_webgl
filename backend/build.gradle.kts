plugins {
    java
    idea
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.flyway)
    alias(libs.plugins.jooq)
    alias(libs.plugins.shadow)
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
    implementation(libs.reflections)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.default.headers)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.gson)
    implementation(libs.slf4j.api)
    implementation(libs.slf4j.log4j12)

    implementation(libs.hikaricp)
    implementation(libs.flyway.core)
    implementation(libs.flyway.mysql)

    runtimeOnly(libs.mariadb.java.client)
    jooqGenerator(libs.mariadb.java.client)
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
        classpath(libs.flyway.mysql)
        classpath(libs.mariadb.java.client)
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
    version.set(libs.versions.jooq.version)

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
                        isRecords = true
                        isImmutablePojos = true
                        isPojosAsKotlinDataClasses = true
                        isKotlinNotNullPojoAttributes = true
                        isKotlinNotNullRecordAttributes = true
                        isDeprecated = false
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