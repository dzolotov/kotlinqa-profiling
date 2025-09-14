# JMH Antipatterns Demo - Kotlin Edition

Демонстрация типичных антипаттернов при написании микробенчмарков с JMH на Kotlin.

## 🎯 Цель демонстрации

Показать, как **НЕ нужно** писать бенчмарки и как исправить типичные ошибки, приводящие к неточным результатам измерений производительности.

## 🚀 Быстрый запуск

```bash
# Интерактивный режим с меню
./run.sh

# Или прямые команды
gradle jmh                    # Полные тесты (5-10 минут)
gradle jmhSmoke              # Быстрые тесты (30 секунд)
```

## 📚 Антипаттерны и решения

### 1. Dead Code Elimination (DCE)

**Проблема**: JVM может исключить код, результат которого не используется.

#### ❌ Неправильно:
```kotlin
@Benchmark
fun wrong() {
    ln(42.0)  // Результат не используется - может быть удален JVM!
}
```

#### ✅ Правильно:
```kotlin
@Benchmark
fun correct(): Double {
    return ln(42.0)  // Результат возвращается
}

@Benchmark
fun correctBlackhole(bh: Blackhole) {
    bh.consume(ln(42.0))  // Blackhole "потребляет" результат
}
```

**Результат измерений**:
- `wrong()` - может показать нереально быстрые результаты (0.1 ns)
- `correct()` - реальное время выполнения ln() (≈ 10-15 ns)

### 2. Constant Folding

**Проблема**: Компилятор заменяет вычисления констант на готовые значения.

#### ❌ Неправильно:
```kotlin
@Benchmark
fun wrongConstantFolding(): Int {
    return 2 * 2  // Компилятор заменит на 4
}
```

#### ✅ Правильно:
```kotlin
private val x = 2
private val y = 2

@Benchmark
fun correctWithFields(): Int {
    return x * y  // Использует поля класса
}

@Param("2", "3", "5")
var parameter: Int = 0

@Benchmark
fun correctWithParam(): Int {
    return parameter * parameter  // Использует параметры
}
```

**Результат измерений**:
- `wrongConstantFolding()` - нереально быстро (0.1 ns)
- `correctWithFields()` - реальное время умножения (≈ 1-2 ns)

### 3. State и Scope Problems

**Проблема**: Неправильное управление состоянием приводит к race conditions или неточным измерениям.

#### ❌ Неправильно - общее состояние:
```kotlin
@State(Scope.Benchmark)  // Разделяется между потоками!
open class WrongState {
    private var counter = 0
    fun increment(): Int = ++counter  // Race condition!
}
```

#### ✅ Правильно - thread-local состояние:
```kotlin
@State(Scope.Thread)  // Каждый поток получает свою копию
open class CorrectState {
    private var counter = 0
    fun increment(): Int = ++counter  // Безопасно
}
```

**Типы Scope**:
- `Benchmark` - одна инстанция для всех потоков (опасно для mutable state)
- `Thread` - отдельная инстанция для каждого потока (безопасно)
- `Group` - одна инстанция для группы бенчмарков (для взаимодействия потоков)

### 4. Kotlin-специфичные проблемы

#### Проблема с лямбдами:
```kotlin
// ❌ Создается новая лямбда каждый раз
@Benchmark
fun wrongLambdaAllocation(): List<String> {
    return testData.map { "item$it" }
}

// ✅ Переиспользуем лямбду
private val mapFunction: (Int) -> String = { "item$it" }

@Benchmark
fun correctPrecomputedLambda(): List<String> {
    return testData.map(mapFunction)
}
```

#### Проблема с collections vs sequences:
```kotlin
// ❌ Создает промежуточные коллекции
@Benchmark
fun wrongExtensionFunction(): List<Int> {
    return testData
        .filter { it % 2 == 0 }  // Создает список
        .map { it * 2 }          // Создает еще один список
        .take(10)                // И еще один
}

// ✅ Ленивые вычисления
@Benchmark
fun correctSequenceChain(): List<Int> {
    return testData.asSequence()
        .filter { it % 2 == 0 }
        .map { it * 2 }
        .take(10)
        .toList()               // Только один список в конце
}
```

## 🔧 Структура проекта

```
03-jmh-problems/
├── src/main/kotlin/
│   ├── DeadCodeElimination.kt      # DCE антипаттерны
│   ├── ConstantFolding.kt          # Constant folding
│   ├── StateAndScope.kt            # State management
│   └── KotlinSpecificProblems.kt   # Kotlin-специфичные
├── build.gradle.kts                # Конфигурация JMH
├── run.sh                          # Интерактивный запуск
└── README.md                       # Эта документация
```

## 📊 Интерпретация результатов

### Пример вывода JMH:
```
Benchmark                                Mode  Cnt      Score       Error  Units
DeadCodeElimination.wrong               avgt    3      0.128 ±     0.001  ns/op
DeadCodeElimination.correct             avgt    3     12.456 ±     0.234  ns/op
DeadCodeElimination.correctBlackhole    avgt    3     12.398 ±     0.156  ns/op
```

### Что это означает:
- **wrong**: 0.1 ns - нереально быстро! JVM исключила код
- **correct**: 12.4 ns - реальное время выполнения ln()
- **correctBlackhole**: 12.4 ns - то же время, что и correct

### Красные флаги:
- ⚠️ Время < 1 ns - вероятно, DCE или constant folding
- ⚠️ Огромная разница между "правильными" и "неправильными" методами
- ⚠️ Error больше 50% от Score - нестабильные измерения

## 🎛️ Настройка JMH

### Основные параметры:
```kotlin
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
```

### Режимы измерения:
- `AverageTime` - среднее время выполнения (по умолчанию)
- `Throughput` - количество операций в секунду
- `SampleTime` - измерение времени с процентилями
- `SingleShotTime` - время "холодного" старта

### Профайлеры:
```bash
# GC профайлер
java -jar build/libs/jmh.jar -prof gc

# Профайлер стека
java -jar build/libs/jmh.jar -prof stack

# Профайлер ветвлений
java -jar build/libs/jmh.jar -prof perf
```

## ⚡ Практические советы

### 1. Всегда проверяйте результаты
- Если время < 1 ns - что-то не так
- Сравните "правильную" и "неправильную" реализации
- Используйте профайлеры для понимания происходящего

### 2. Blackhole - ваш друг
```kotlin
// Когда нужно "потребить" результат, но не возвращать
@Benchmark
fun test(bh: Blackhole) {
    val result = heavyCalculation()
    bh.consume(result)
}
```

### 3. Параметризация
```kotlin
@Param("100", "1000", "10000")
var size: Int = 0

// Проверяет масштабируемость алгоритма
```

### 4. State management
- Используйте `@State(Scope.Thread)` для mutable состояния
- `@State(Scope.Benchmark)` только для immutable данных
- `@Setup` и `@TearDown` для инициализации/очистки

## 🔍 Troubleshooting

### Частые проблемы:

1. **"No benchmarks to run"**
   - Проверьте аннотации `@Benchmark`
   - Убедитесь, что классы `open`

2. **Compilation errors**
   - Добавьте plugin `allopen` для JMH аннотаций
   - Используйте правильные импорты JMH

3. **Нестабильные результаты**
   - Увеличьте количество warmup итераций
   - Закройте другие приложения
   - Используйте `-XX:+UseConcMarkSweepGC`

4. **OutOfMemoryError**
   - Увеличьте heap: `-Xmx4g`
   - Уменьшите размер тестовых данных

## 📈 Анализ результатов

### Используйте статистику:
- **Score** - среднее время
- **Error** - доверительный интервал (99.9%)
- **Cnt** - количество измерений

### Сравнивайте осмысленно:
```
Method A: 100 ± 5 ns/op
Method B: 95 ± 10 ns/op
```
Разница не значима - error intervals пересекаются!

## 🎓 Образовательные цели

После изучения примеров вы будете знать:

1. **Как JVM оптимизирует код** и почему это ломает бенчмарки
2. **Типичные ошибки** при написании микробенчмарков
3. **Правильные паттерны** для точных измерений
4. **Kotlin-специфичные проблемы** производительности
5. **Как интерпретировать результаты** JMH

## 🚀 Следующие шаги

1. Запустите все примеры и сравните результаты
2. Попробуйте написать свои "неправильные" бенчмарки
3. Изучите profiler output для понимания оптимизаций
4. Примените знания к реальному коду

---

💡 **Помните**: Цель бенчмарка - измерить реальную производительность, а не скорость оптимизаций компилятора!