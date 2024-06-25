plugins {
    val kotlinVersion = "1.9.24" // https://github.com/JetBrains/kotlin/releases

    java
    idea
    kotlin("jvm") version kotlinVersion
}

val ktorVersion = "2.3.12" // https://kotlinlang.org/
val slf4jVersion = "2.0.13" // https://mvnrepository.com/artifact/org.slf4j/slf4j-api

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

//tasks.withType<KotlinCompile> {
//    kotlinOptions.jvmTarget = "17"
//}


//compileKotlin {
//    kotlinOptions.jvmTarget = "17"
//}
//compileTestKotlin {
//    kotlinOptions.jvmTarget = "17"
//}

repositories {
    mavenCentral()
}
//
//buildscript {
//    ext {
//        kotlin_version = '1.9.23'
//        ktor_version = '2.3.12' // https://ktor.io/
//        exposed_version = '0.49.0' // https://github.com/JetBrains/Exposed
//        slf4j_version = '2.0.13' // https://mvnrepository.com/artifact/org.slf4j/slf4j-api
//    }
//
//    repositories {
//        mavenCentral()
//        maven {
//            url "https://plugins.gradle.org/m2/"
//        }
//    }
//    dependencies {
//        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
//    }
//}
//


dependencies {
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
//
//    implementation "org.jetbrains.exposed:exposed-core:$exposed_version"
//    implementation "org.jetbrains.exposed:exposed-jdbc:$exposed_version"
//    implementation "org.jetbrains.exposed:exposed-dao:$exposed_version"
//    implementation "org.jetbrains.exposed:exposed-jodatime:$exposed_version"
//
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-log4j12:$slf4jVersion")
//
    implementation("com.google.code.gson:gson:2.11.0") // https://mvnrepository.com/artifact/com.google.code.gson/gson
//
//    implementation 'com.zaxxer:HikariCP:5.1.0' // https://mvnrepository.com/artifact/com.zaxxer/HikariCP
//    implementation 'org.mariadb.jdbc:mariadb-java-client:3.0.9'
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
