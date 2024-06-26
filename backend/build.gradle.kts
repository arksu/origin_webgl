import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion = "2.3.12" // https://kotlinlang.org/
val slf4jVersion = "2.0.13" // https://mvnrepository.com/artifact/org.slf4j/slf4j-api

plugins {
    val kotlinVersion = "1.9.24" // https://github.com/JetBrains/kotlin/releases

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

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}


//compileTestKotlin {
//    kotlinOptions.jvmTarget = "17"
//}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
//    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
//
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
//
    implementation("com.google.code.gson:gson:2.11.0") // https://mvnrepository.com/artifact/com.google.code.gson/gson
//
//    implementation 'com.zaxxer:HikariCP:5.1.0' // https://mvnrepository.com/artifact/com.zaxxer/HikariCP
    implementation("org.flywaydb:flyway-core:10.15.0")
    runtimeOnly("org.flywaydb:flyway-mysql:10.15.0")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client:3.4.0") // https://mvnrepository.com/artifact/org.mariadb.jdbc/mariadb-java-client

    jooqGenerator("org.mariadb.jdbc:mariadb-java-client:3.4.0")

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
    version.set("3.19.10")
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)

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
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
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