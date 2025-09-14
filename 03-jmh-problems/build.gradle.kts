plugins {
    kotlin("jvm") version "2.0.20"
    id("me.champeau.jmh") version "0.7.2"
    kotlin("plugin.allopen") version "2.0.20"
}

group = "com.example.jmh.problems"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.openjdk.jmh:jmh-core:1.37")
    annotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.37")

    // JMH dependencies for gradle plugin
    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")
}

// Настройка для работы с аннотациями JMH в Kotlin
allOpen {
    annotation("org.openjdk.jmh.annotations.State")
    annotation("org.openjdk.jmh.annotations.BenchmarkMode")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

jmh {
    // Версия JMH
    jmhVersion.set("1.37")

    // Включить все бенчмарки
    includes.set(listOf(".*"))

    // Количество fork процессов
    fork.set(1)

    // Количество итераций прогрева
    warmupIterations.set(5)

    // Количество измерительных итераций
    iterations.set(10)

    // Режим бенчмарка
    benchmarkMode.set(listOf("AverageTime"))

    // Единица времени вывода
    timeUnit.set("ns")

    // JVM аргументы
    jvmArgs.set(listOf(
        "-server",
        "-Xms2g",
        "-Xmx2g"
    ))

    // Формат вывода результатов
    resultFormat.set("TEXT")

    // Файл для сохранения результатов
    resultsFile.set(project.file("build/reports/jmh/results.txt"))

    // Детализированный вывод
    verbosity.set("NORMAL")
}

tasks {
    // Настройка компиляции Kotlin
    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }
}