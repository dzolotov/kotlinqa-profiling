# 00. Основы измерения времени в Kotlin

## 📋 Обзор

Этот пример демонстрирует фундаментальные принципы измерения времени выполнения в Kotlin. Показывает разницу между `measureTimeMillis` и `measureNanoTime`, проблемы монотонного времени, и правильные подходы к измерению производительности.

## 🎯 Цели примера

1. **Понять разницу** между различными методами измерения времени
2. **Изучить проблему монотонного времени** и способы её решения
3. **Освоить правильные техники** для точных измерений
4. **Понять ограничения** каждого подхода к измерению времени

## 📁 Структура примера

```
00-time-measurements/
├── TimeMeasurementsDemo.kt       # Основные концепции измерения времени
├── AdvancedTimingExamples.kt     # Продвинутые техники и подводные камни
├── build.gradle.kts              # Конфигурация сборки
└── README.md                     # Документация
```

## 🚀 Запуск примеров

### Компиляция и запуск:

```bash
# Компилируем основной пример
kotlinc TimeMeasurementsDemo.kt -include-runtime -d TimeMeasurementsDemo.jar

# Запускаем
java -jar TimeMeasurementsDemo.jar

# Компилируем продвинутый пример
kotlinc AdvancedTimingExamples.kt -include-runtime -d AdvancedTimingExamples.jar

# Запускаем
java -jar AdvancedTimingExamples.jar
```

### Через Gradle:

```bash
# Основной пример
./gradlew runBasicDemo

# Продвинутый пример
./gradlew runAdvancedDemo
```

## 📊 Основные концепции

### 1. measureTimeMillis vs measureNanoTime

#### measureTimeMillis - для медленных операций (>1мс):
```kotlin
val time = measureTimeMillis {
    Thread.sleep(100) // 100мс операция
    // Сложные вычисления...
}
println("Время выполнения: ${time}мс")
```

**Подходит для:**
- Операций длительностью > 1мс
- Измерения времени I/O операций
- Общего профилирования приложения

**Проблемы:**
- Плохая точность для быстрых операций
- Может показывать 0мс для микрооперций

#### measureNanoTime - для быстрых операций:
```kotlin
val time = measureNanoTime {
    repeat(1000) {
        val result = it * it + it
    }
}
println("Время: ${time}нс = ${time/1_000_000.0}мс")
```

**Подходит для:**
- Микробенчмарков
- Измерения быстрых алгоритмов
- Точного профилирования

### 2. Проблема монотонного времени

#### ❌ НЕПРАВИЛЬНО - System.currentTimeMillis():
```kotlin
// ПЛОХО: может давать некорректные результаты!
val start = System.currentTimeMillis()
performOperation()
val end = System.currentTimeMillis()
val duration = end - start // Может быть отрицательным!
```

**Проблемы:**
- Может "прыгать" при NTP синхронизации
- Зависит от системного времени
- Может идти назад при корректировке часов
- Влияет переход на летнее/зимнее время

#### ✅ ПРАВИЛЬНО - System.nanoTime():
```kotlin
// ХОРОШО: монотонное время
val start = System.nanoTime()
performOperation()
val end = System.nanoTime()
val duration = end - start // Всегда корректно!
```

**Преимущества:**
- Монотонно возрастающее время
- Не зависит от системного времени
- Высокая точность (наносекунды)
- Идеально для измерения интервалов

## 🔬 Продвинутые техники

### 1. Прогрев JVM

```kotlin
// Прогрев перед измерениями
repeat(1000) {
    performOperation() // JVM оптимизирует код
}

// Теперь точные измерения
val time = measureNanoTime {
    performOperation()
}
```

**Почему важно:**
- JIT компилятор оптимизирует "горячий" код
- Первые выполнения медленнее
- Результаты стабилизируются после прогрева

### 2. Статистический подход

```kotlin
val measurements = mutableListOf<Long>()

// Множественные измерения
repeat(50) {
    val time = measureNanoTime { performOperation() }
    measurements.add(time)
}

// Статистический анализ
val mean = measurements.average()
val median = measurements.sorted()[measurements.size / 2]
val stdDev = calculateStandardDeviation(measurements)
```

**Метрики:**
- **Среднее** - общая производительность
- **Медиана** - устойчивость к выбросам
- **Стандартное отклонение** - стабильность
- **Перцентили** - распределение времени

### 3. Влияние Garbage Collector

```kotlin
// Операция, создающая много объектов
val time = measureNanoTime {
    val list = mutableListOf<String>()
    repeat(100_000) {
        list.add("String $it") // Создаем мусор
    }
}
```

**Проблемы GC:**
- Непредсказуемые паузы
- Искажение результатов измерений
- Выбросы во времени выполнения

**Решения:**
- Множественные измерения
- Исключение выбросов
- Анализ перцентилей

## 📈 Примеры использования

### Сравнение алгоритмов:
```kotlin
fun compareSort() {
    val data = (1..10000).shuffled()

    val bubbleTime = measureNanoTime {
        bubbleSort(data.toMutableList())
    }

    val quickTime = measureNanoTime {
        data.sorted() // Optimized sort
    }

    println("Bubble sort: ${bubbleTime/1_000_000.0}мс")
    println("Quick sort:  ${quickTime/1_000_000.0}мс")
    println("Ускорение: ${bubbleTime.toDouble()/quickTime}x")
}
```

### Профилирование I/O операций:
```kotlin
fun profileFileOperations() {
    val readTime = measureTimeMillis {
        File("data.txt").readText()
    }

    val writeTime = measureTimeMillis {
        File("output.txt").writeText("data")
    }

    println("Чтение: ${readTime}мс")
    println("Запись: ${writeTime}мс")
}
```

### Микробенчмарки:
```kotlin
fun microbenchmark() {
    // Прогрев
    repeat(10_000) { stringConcatenation() }

    // Измерение
    val times = mutableListOf<Long>()
    repeat(1000) {
        val time = measureNanoTime { stringConcatenation() }
        times.add(time)
    }

    val avgTime = times.average()
    println("Среднее время: ${avgTime/1000.0}мкс")
}
```

## ⚠️ Подводные камни

### 1. Недостаточное разрешение времени
```kotlin
// Слишком быстрая операция
val time = measureTimeMillis {
    val sum = (1..100).sum() // Микросекунды
}
// Результат: 0мс (неинформативно!)
```

### 2. Влияние системной нагрузки
- Другие процессы
- Системные прерывания
- Thermal throttling

### 3. Compiler optimizations
```kotlin
// Может быть оптимизировано компилятором!
val time = measureNanoTime {
    val result = 2 + 2 // Константа времени компиляции
}
// Результат может быть ~0нс
```

## 🎯 Рекомендации

### Когда использовать measureTimeMillis:
- ✅ I/O операции (файлы, сеть)
- ✅ Операции > 1мс
- ✅ Общее профилирование
- ✅ User-facing время отклика

### Когда использовать measureNanoTime:
- ✅ Микробенчмарки
- ✅ Алгоритмические операции
- ✅ Операции < 1мс
- ✅ Точные измерения производительности

### Общие принципы:
1. **Прогревайте JVM** перед измерениями
2. **Делайте множественные измерения** и усредняйте
3. **Исключайте выбросы** от GC и системной нагрузки
4. **Используйте статистический анализ** результатов
5. **Документируйте условия** измерений

## 🔗 Связь с другими примерами

- **Пример 01** - Профилировщики используют те же принципы
- **Пример 03** - JMH реализует эти техники профессионально
- **Пример 06** - Jetpack Benchmark использует правильное измерение времени
- **Пример 07** - CI/CD использует результаты измерений для regression testing

## 📚 Дополнительные материалы

### Официальная документация:
- [Kotlin measureTimeMillis](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.system/measure-time-millis.html)
- [System.nanoTime() JavaDoc](https://docs.oracle.com/javase/8/docs/api/java/lang/System.html#nanoTime--)

### Рекомендуемое чтение:
- "Java Performance" by Binu John
- "Systems Performance" by Brendan Gregg
- JMH documentation и best practices

## 🎉 Заключение

Правильное измерение времени - это **основа** всех инструментов профилирования. Понимание этих принципов поможет:

- ✅ Делать **точные** измерения производительности
- ✅ **Избегать** типичных ошибок в бенчмарках
- ✅ **Интерпретировать** результаты профилировщиков
- ✅ **Выбирать** подходящие инструменты для задач

Эти знания станут фундаментом для изучения более сложных инструментов профилирования в следующих примерах!