# Kotlinx.benchmark Demo

Современные микробенчмарки для Kotlin с использованием kotlinx.benchmark 0.4.14.

## 🎯 Что демонстрирует

### Основные бенчмарки (`ModernBenchmark`)
- **Collections vs Sequences** - сравнение производительности
- **filter + map** операций в разных вариантах
- **Суммирование** - последовательное vs параллельное

### GC-интенсивные бенчмарки (`GCBenchmark`)
- **String concatenation** - `joinToString` vs `StringBuilder`
- **Memory allocation** - различные подходы к созданию коллекций
- **GC pressure** - влияние аллокаций на сборку мусора

## 🚀 Запуск

### Интерактивный режим
```bash
./run.sh
```

### Прямые команды
```bash
# Быстрые бенчмарки (500ms итерации)
gradle jvmSmokeBenchmark

# Полные бенчмарки (10s итерации)
gradle jvmBenchmark

# Только компиляция
gradle jvmBenchmarkCompile
```

## 📊 Результаты

После запуска результаты сохраняются в:
- `build/reports/benchmarks/smoke/` - быстрые тесты
- `build/reports/benchmarks/main/` - полные тесты

### Пример выходных данных:
```
Benchmark                          (size)  Mode  Cnt      Score       Error  Units
GCBenchmark.allocateList             1000  avgt    3      0,008 ±     0,001  ms/op
GCBenchmark.allocateStringBuilder    1000  avgt    3      0,009 ±     0,005  ms/op
GCBenchmark.allocateStrings          1000  avgt    3      0,014 ±     0,001  ms/op
ModernBenchmark.filterMap             100  avgt    3    684,910 ±   336,644  ns/op
ModernBenchmark.sequence              100  avgt    3    633,058 ±   121,195  ns/op
ModernBenchmark.sumSequential         100  avgt    3     37,438 ±     6,688  ns/op
```

## 🔧 Конфигурация

### Build Configuration
```kotlin
plugins {
    kotlin("multiplatform") version "2.0.20"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.14"
    kotlin("plugin.allopen") version "2.0.20"
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
```

### Benchmark Annotations
```kotlin
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
class ModernBenchmark {

    @Param("100", "1000", "10000")
    var size: Int = 0

    @Setup
    fun setup() {
        // Инициализация перед каждым бенчмарком
    }

    @Benchmark
    fun myBenchmark(): Any {
        // Код для измерения
    }
}
```

## 📈 Анализ результатов

### Интерпретация метрик:
- **Score** - среднее время выполнения
- **Error** - доверительный интервал (99.9%)
- **Units** - единицы измерения (ns/op, ms/op)
- **Mode** - режим измерения (AverageTime, Throughput)

### Что показывают бенчмарки:

1. **Collections vs Sequences**
   ```kotlin
   // Collections (eager evaluation)
   data.filter { it % 2 == 0 }.map { "item$it" }

   // Sequences (lazy evaluation)
   data.asSequence()
       .filter { it % 2 == 0 }
       .map { "item$it" }
       .toList()
   ```

2. **String Building Performance**
   ```kotlin
   // Медленно - много аллокаций
   (0 until size).joinToString { "item$it" }

   // Быстрее - одна аллокация
   StringBuilder().apply {
       repeat(size) { append("item$it") }
   }.toString()
   ```

3. **Memory Allocation Patterns**
   - `allocateList` - создание коллекции строк
   - `allocateStringBuilder` - оптимизированное создание строк
   - `allocateStrings` - простая конкатенация

## 🎓 Образовательные цели

### Уровень 1: Основы
- Настройка kotlinx.benchmark
- Написание первых бенчмарков
- Понимание базовых метрик

### Уровень 2: Анализ
- Сравнение различных подходов
- Интерпретация статистических данных
- Влияние размера данных на производительность

### Уровень 3: Оптимизация
- Анализ GC pressure
- Memory allocation patterns
- Collections vs Sequences trade-offs

## 💡 Полезные команды

### Gradle Tasks
```bash
# Показать все benchmark задачи
gradle tasks --group=benchmark

# Генерация JMH кода
gradle jvmBenchmarkGenerate

# Компиляция бенчмарков
gradle jvmBenchmarkCompile

# Создание JAR для бенчмарков
gradle jvmBenchmarkJar
```

### Анализ отчетов
```bash
# Показать JSON отчет
cat build/reports/benchmarks/main/*/jvm.json | jq

# Найти самые медленные бенчмарки
grep -E "Score|Error" build/reports/benchmarks/main/*/jvm.json

# Сравнить результаты разных запусков
diff build/reports/benchmarks/main/*/jvm.json \
     build/reports/benchmarks/smoke/*/jvm.json
```

## 🔍 Troubleshooting

### Частые проблемы:

1. **"Benchmark class should have package other than default"**
   - Добавьте `package` декларацию в начало файла

2. **"Plugin not found"**
   - Укажите версии всех плагинов явно

3. **Compilation errors**
   - Используйте только `kotlinx.benchmark.*` импорты
   - Не смешивайте с `org.openjdk.jmh.annotations.*`

4. **Out of memory**
   - Уменьшите размер параметров `@Param`
   - Используйте smoke конфигурацию для тестирования

### Debug режим:
```bash
# Детальный вывод
gradle jvmSmokeBenchmark --info

# Отладочная информация
gradle jvmSmokeBenchmark --debug

# Профилирование сборки
gradle jvmSmokeBenchmark --profile
```

## 📚 Дополнительные материалы

- [Kotlinx.benchmark Documentation](https://github.com/Kotlin/kotlinx-benchmark)
- [JMH Samples](https://github.com/openjdk/jmh/tree/master/jmh-samples/src/main/java/org/openjdk/jmh/samples)
- [Kotlin Collections Performance](https://kotlinlang.org/docs/collections-overview.html#sequence)

## 🎯 Следующие шаги

1. Модифицируйте существующие бенчмарки
2. Добавьте свои тесты производительности
3. Экспериментируйте с различными JVM параметрами
4. Сравните результаты на разных платформах