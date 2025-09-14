plugins {
    kotlin("multiplatform") version "2.0.20"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.14"
    kotlin("plugin.allopen") version "2.0.20"
}

repositories {
    mavenCentral()
}

kotlin {
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.14")
            }
        }
    }
}

allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

benchmark {
    targets {
        register("jvm")
    }

    configurations {
        named("main") {
            iterations = 3
            iterationTime = 10
            iterationTimeUnit = "sec"
        }

        register("smoke") {
            warmups = 5
            iterations = 3
            iterationTime = 500
            iterationTimeUnit = "ms"
        }
    }
}