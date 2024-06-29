import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion = "2.3.12" // https://kotlinlang.org/
val slf4jVersion = "2.0.13" // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
val flywayVersion = "10.15.0" // https://plugins.gradle.org/plugin/org.flywaydb.flyway
val mariadbJavaClientVersion = "3.4.0" // https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client
val jooqVersion = "3.19.10" // https://mvnrepository.com/artifact/org.jooq/jooq

plugins {
    val kotlinVersion = "2.0.0" // https://github.com/JetBrains/kotlin/releases

    java
    idea
    application
    kotlin("jvm") version kotlinVersion
    id("org.flywaydb.flyway") version "10.15.0" // https://plugins.gradle.org/plugin/org.flywaydb.flyway
    id("nu.studer.jooq") version "9.0" // https://plugins.gradle.org/plugin/nu.studer.jooq
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

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-default-headers:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")
    implementation("io.ktor:ktor-serialization-gson:$ktorVersion")

    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-log4j12:$slf4jVersion")

    implementation("com.google.code.gson:gson:2.11.0") // https://mvnrepository.com/artifact/com.google.code.gson/gson

    implementation("com.zaxxer:HikariCP:5.1.0") // https://mvnrepository.com/artifact/com.zaxxer/HikariCP
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    runtimeOnly("org.flywaydb:flyway-mysql:$flywayVersion")

    runtimeOnly("org.mariadb.jdbc:mariadb-java-client:$mariadbJavaClientVersion")
    jooqGenerator("org.mariadb.jdbc:mariadb-java-client:$mariadbJavaClientVersion")

//    implementation 'com.typesafe:config:1.4.2'
}
//
//task mapgen(type: JavaExec) {
//    classpath = sourceSets.main.runtimeClasspath
//    main = 'com.origin.utils.MapGenerator'
//}
//task mapimport(type: JavaExec) {
//    classpath = sourceSets.main.runtimeClasspath
//    main = 'com.origin.utils.MapImporter'
//}
//
//jar {
//    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//
//    manifest {
//        def manifestClasspath =
//                configurations.compileClasspath.collect { it.name }.join(' ')
//
//        attributes 'Implementation-Title': 'origin',
//                'Main-Class': 'com.origin.ServerLauncher',
//                'Class-Path': manifestClasspath
//    }
//
//    from {
//        configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) }
//    }
//}

buildscript {
    dependencies {
        classpath("org.flywaydb:flyway-mysql:10.15.0")
        classpath("org.mariadb.jdbc:mariadb-java-client:3.4.0")
    }
}

flyway {
    url = "jdbc:mariadb://localhost:3406/origin"
    user = "origin"
    password = "origin"
    schemas = arrayOf("origin")
    cleanDisabled = false
    locations = arrayOf("filesystem:res/db/migration")
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
                    url = "jdbc:mariadb://localhost:3406/origin"
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