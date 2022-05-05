plugins {
    id ("application")
    id ("org.jetbrains.kotlin.jvm") version "1.6.10"
}

group "de.zweistein2"
val buildVersion = "0.0.1"
version "1.0.0"

val kotlinVersion = "1.6.10"
val ktorVersion = "2.0.0"
val logbackVersion = "1.2.11"
val loggingVersion = "2.1.21"
val slf4jVersion = "1.7.36"
val junitVersion = "5.8.2"
val mockitoVersion = "4.4.0"
val kotlinxVersion = "1.6.1"

application {
    mainClass.set("de.zweistein2.ApplicationKt")
}

repositories {
    mavenCentral()
    maven {
        setUrl("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    }
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-host-common:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-websockets:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-log4j12:$slf4jVersion")
    implementation("io.github.microutils:kotlin-logging-jvm:$loggingVersion")
    implementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
}

tasks {
    val fatJar = register<Jar>("fatJar") {
        dependsOn.addAll(listOf("compileJava", "compileKotlin", "processResources")) // We need this for Gradle optimization to work
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest { attributes(mapOf("Main-Class" to application.mainClass)) } // Provided we set it up in the application plugin configuration
        val sourcesMain = sourceSets.main.get()
        val contents = configurations.runtimeClasspath.get()
                .map { if (it.isDirectory) it else zipTree(it) } +
                sourcesMain.output
        from(contents)
    }
    compileKotlin {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
        }
    }
    build {
        dependsOn(fatJar) // Trigger fat jar creation during build
    }
    test {
        useJUnitPlatform()
    }
}