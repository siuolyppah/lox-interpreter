plugins {
    kotlin("jvm") version "1.9.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation(kotlin("stdlib-jdk8"))

    // arrow
    implementation(platform("io.arrow-kt:arrow-stack:1.2.0"))   // bom
    implementation("io.arrow-kt:arrow-core")                    // no versions on libraries
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
    jvmToolchain(8)
}