# Kotlin Multiplatform Benchmarks

Этот пример демонстрирует профилирование производительности в Kotlin Multiplatform проектах с использованием kotlinx.benchmark. Показаны различия в производительности между JVM, JavaScript (Node.js) и Native платформами.

## 🎯 Цель примера

Понять как:
- Настроить kotlinx.benchmark для multiplatform проекта
- Создать платформенно-специфичные оптимизации
- Сравнить производительность между разными таргетами
- Использовать expect/actual механизм для оптимизаций

## 📁 Структура проекта

```
04-multiplatform/
├── build.gradle.kts          # Настройка multiplatform + benchmarks
├── run.sh                    # Интерактивный скрипт запуска
├── README.md
└── src/
    ├── commonMain/kotlin/
    │   ├── CollectionBenchmark.kt    # Общие бенчмарки коллекций
    │   ├── PlatformBenchmark.kt      # Сравнение платформенных оптимизаций
    │   └── PlatformOptimizer.kt      # expect класс для оптимизаций
    ├── jvmMain/kotlin/
    │   └── PlatformOptimizer.jvm.kt  # JVM реализация с параллелизмом
    ├── jsMain/kotlin/
    │   └── PlatformOptimizer.js.kt   # JavaScript реализация для V8
    ├── linuxX64Main/kotlin/
    │   └── PlatformOptimizer.linux.kt # Native Linux реализация
    ├── macosX64Main/kotlin/
    │   └── PlatformOptimizer.macos.kt # Native macOS x64 реализация
    └── macosArm64Main/kotlin/
        └── PlatformOptimizer.macos.kt # Native macOS ARM64 реализация
```

## 🏗️ Конфигурация

### Gradle настройка

```kotlin
kotlin {
    jvm()
    js(IR) { nodejs() }
    linuxX64()
    macosX64()
    macosArm64()
}

benchmark {
    targets {
        register("jvm")
        register("js")
        register("linuxX64")
        register("macosX64")
        register("macosArm64")
    }

    configurations {
        register("smoke") {
            warmups = 3
            iterations = 5
            iterationTime = 500
            iterationTimeUnit = "ms"
        }
    }
}
```

### Платформенные таргеты

- **JVM**: Java 17, использует ForkJoinPool для параллелизма
- **JavaScript**: Node.js, оптимизации для V8 engine
- **Native Linux**: LLVM оптимизации, минимальные аллокации
- **Native macOS**: Foundation API, векторизация

## 🔬 Тестируемые аспекты

### 1. Коллекции (CollectionBenchmark)
```kotlin
@Benchmark
fun filterMap(): List<String> =
    data.filter { it % 2 == 0 }.map { "item$it" }

@Benchmark
fun sequence(): List<String> = data.asSequence()
    .filter { it % 2 == 0 }
    .map { "item$it" }
    .toList()
```

### 2. Платформенные оптимизации (PlatformBenchmark)

#### JVM реализация
- Параллельные стримы для больших коллекций
- ForkJoinPool для heavy computations
- RecursiveTask для divide-and-conquer

#### JavaScript реализация
- Оптимизации для V8 engine
- StringBuilder для эффективной конкатенации
- Избежание промежуточных коллекций

#### Native реализация
- LLVM оптимизации
- Insertion sort для малых массивов
- Foundation API для macOS
- Векторизация циклов

## 🚀 Запуск бенчмарков

### Интерактивный режим
```bash
./run.sh
```

Меню:
1. **JVM тесты** - быстрые тесты на JVM
2. **JavaScript тесты** - тесты в Node.js
3. **Native тесты** - нативная компиляция
4. **Все платформы** - полное сравнение
5. **Smoke тесты** - быстрая проверка всех платформ

### Прямые команды Gradle

```bash
# JVM бенчмарки
./gradlew benchmark --configuration-name=smoke

# JavaScript бенчмарки
./gradlew jsBenchmark --configuration-name=smoke

# Native бенчмарки (macOS)
./gradlew macosX64Benchmark --configuration-name=smoke
./gradlew macosArm64Benchmark --configuration-name=smoke

# Все платформы
./gradlew benchmark jsBenchmark macosX64Benchmark
```

## 📊 Ожидаемые результаты

### Collection Benchmarks

| Operation | JVM | JS | Native | Комментарий |
|-----------|-----|-----|--------|-------------|
| filterMap | ~50μs | ~80μs | ~30μs | Native fastest |
| sequence | ~45μs | ~75μs | ~25μs | Lazy evaluation wins |

### Platform Optimizations

| Operation | Standard | JVM Optimized | JS Optimized | Native Optimized |
|-----------|----------|---------------|--------------|------------------|
| Sort 100K | ~50ms | ~20ms | ~60ms | ~15ms |
| String concat | ~10ms | ~5ms | ~8ms | ~3ms |
| Heavy computation | ~100ms | ~40ms | ~120ms | ~80ms |

## 🔍 Интересные находки

### JVM
- Параллельные стримы эффективны для больших коллекций (>1000 элементов)
- ForkJoinPool хорошо масштабируется на многоядерных системах
- Overhead создания задач компенсируется при n>1000

### JavaScript
- V8 отлично оптимизирует простые циклы
- Array methods работают быстрее ручных циклов
- StringBuilder эффективнее многократной конкатенации

### Native
- LLVM генерирует очень быстрый код
- Insertion sort побеждает для малых массивов (<50)
- Foundation API на macOS может быть медленнее прямых вычислений

## 🛠️ Требования

- Kotlin 2.0.20+
- Gradle 8.4+
- JDK 17
- Node.js (для JS тестов)
- Xcode Command Line Tools (для macOS Native)

## ⚠️ Важные замечания

1. **Warmup важен**: Особенно для JVM (JIT компиляция)
2. **Размер данных**: Оптимизации эффективны от определенного размера
3. **Платформенные различия**: Не все оптимизации переносимы
4. **Memory layout**: Native код более предсказуем по памяти

## 📚 Полезные ссылки

- [kotlinx.benchmark документация](https://github.com/Kotlin/kotlinx-benchmark)
- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Kotlin/Native performance](https://kotlinlang.org/docs/native-performance.html)

## 🤔 Вопросы для размышления

1. Почему Native код не всегда самый быстрый?
2. Когда оправдана сложность платформенных оптимизаций?
3. Как измерить реальную производительность в production?
4. Какие компромиссы между производительностью и читаемостью кода?