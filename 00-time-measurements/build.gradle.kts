plugins {
    kotlin("jvm") version "2.2.20"
    application
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
}

kotlin {
    jvmToolchain(17)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// Таск для запуска основного примера
tasks.register<JavaExec>("runBasicDemo") {
    group = "application"
    description = "Запуск базового примера измерения времени"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("TimeMeasurementsDemo")

    // JVM параметры для лучшего измерения времени
    jvmArgs = listOf(
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+UseG1GC",
        "-XX:MaxGCPauseMillis=200",
        "-Xmx1g",
        "-Xms512m"
    )
}

// Таск для запуска продвинутого примера
tasks.register<JavaExec>("runAdvancedDemo") {
    group = "application"
    description = "Запуск продвинутого примера измерения времени"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("AdvancedTimingExamples")

    // JVM параметры для демонстрации различных эффектов
    jvmArgs = listOf(
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+UseG1GC",
        "-XX:MaxGCPauseMillis=200",
        "-Xmx1g",
        "-Xms512m"
    )
}

// Таск для компиляции JAR файлов с основным классом
tasks.jar {
    manifest {
        attributes["Main-Class"] = "TimeMeasurementsDemo"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Создание отдельных JAR файлов для каждого примера
tasks.register<Jar>("basicDemoJar") {
    group = "build"
    description = "Создание JAR для базового примера"
    archiveClassifier.set("basic")
    manifest {
        attributes["Main-Class"] = "TimeMeasurementsDemo"
    }
    from(sourceSets["main"].output)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.register<Jar>("advancedDemoJar") {
    group = "build"
    description = "Создание JAR для продвинутого примера"
    archiveClassifier.set("advanced")
    manifest {
        attributes["Main-Class"] = "AdvancedTimingExamples"
    }
    from(sourceSets["main"].output)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Настройка application plugin
application {
    mainClass.set("TimeMeasurementsDemo")

    // JVM аргументы для более стабильных измерений
    applicationDefaultJvmArgs = listOf(
        "-XX:+UnlockExperimentalVMOptions",
        "-XX:+UseG1GC",
        "-XX:MaxGCPauseMillis=200",
        "-Xmx1g",
        "-Xms512m"
    )
}

// Таск для демонстрации всех примеров подряд
tasks.register("runAllDemos") {
    group = "application"
    description = "Запуск всех примеров измерения времени последовательно"
    dependsOn("runBasicDemo", "runAdvancedDemo")

    doLast {
        println("\n" + "=".repeat(60))
        println("✅ Все примеры измерения времени выполнены!")
        println("=".repeat(60))
        println("\n📋 Что мы изучили:")
        println("• Разница между measureTimeMillis и measureNanoTime")
        println("• Проблему монотонного времени (System.currentTimeMillis vs System.nanoTime)")
        println("• Важность прогрева JVM для точных измерений")
        println("• Влияние Garbage Collector на результаты")
        println("• Статистический подход к измерениям")
        println("• Ограничения разрешения времени")
        println("\n🎯 Теперь вы готовы к изучению профессиональных инструментов профилирования!")
    }
}

// Настройки для улучшения качества измерений
tasks.withType<JavaExec> {
    // Стандартное время прогрева JVM
    systemProperty("warmup.iterations", "1000")

    // Настройки для более стабильных результатов
    systemProperty("java.awt.headless", "true")

    // Отключение неявного GC во время измерений
    jvmArgs("-XX:+DisableExplicitGC")
}