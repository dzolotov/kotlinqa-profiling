plugins {
    kotlin("multiplatform") version "2.2.20"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.14"
    kotlin("plugin.allopen") version "2.2.20"
}

group = "com.example.multiplatform.benchmark"
version = "1.0.0"

repositories {
    mavenCentral()
}

kotlin {
    // JVM target
    jvm {
        compilations.all {
            compilerOptions.configure {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
            }
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    // JavaScript target (Node.js)
    js(IR) {
        nodejs()
        binaries.executable()
    }

    // Native targets
    linuxX64()
    macosX64()
    macosArm64()


    sourceSets {
        // Common source set
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.14")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            }
        }

        // JVM-specific dependencies
        val jvmMain by getting {
            dependencies {
                // No need for java.base - it's implicit
            }
        }

        // JS-specific dependencies
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.8.1")
            }
        }

        // Native shared source set
        val nativeMain by creating {
            dependsOn(commonMain)
        }

        val linuxX64Main by getting {
            dependsOn(nativeMain)
        }

        val macosX64Main by getting {
            dependsOn(nativeMain)
        }

        val macosArm64Main by getting {
            dependsOn(nativeMain)
        }
    }
}

// Plugin configuration для allOpen
allOpen {
    annotation("kotlinx.benchmark.State")
    annotation("org.openjdk.jmh.annotations.State")
}

// Benchmark configuration
benchmark {
    targets {
        register("jvm")
        register("js")
        // Native targets
        register("linuxX64")
        register("macosX64")
        register("macosArm64")
    }

    configurations {
        // Main configuration - полные тесты
        named("main") {
            warmups = 5
            iterations = 10
            iterationTime = 3
            iterationTimeUnit = "s"

            // JVM-specific options
            advanced("jvmForks", "1")
        }

        // Smoke configuration - быстрые тесты
        register("smoke") {
            warmups = 3
            iterations = 5
            iterationTime = 500
            iterationTimeUnit = "ms"

            // Include patterns
            include(".*Collection.*")
            include(".*Platform.*")
        }

        // Comparison configuration - для сравнения платформ
        register("comparison") {
            warmups = 2
            iterations = 3
            iterationTime = 1
            iterationTimeUnit = "s"

            // Focus on platform differences
            include(".*PlatformBenchmark.*")
        }
    }
}

// Gradle wrapper
tasks.wrapper {
    gradleVersion = "8.4"
}

// Configure JVM target
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}