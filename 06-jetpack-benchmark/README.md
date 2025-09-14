# Jetpack Benchmark Demo

Этот проект демонстрирует использование **Jetpack Benchmark** для измерения производительности Android приложений с **полной автоматизацией** процесса тестирования и извлечения результатов.

## Структура проекта

```
06-jetpack-benchmark/
├── app/                    # Основное приложение (target для бенчмарков)
├── benchmark/              # Микро-бенчмарки (измерение кода)
└── macrobenchmark/         # Макро-бенчмарки (измерение UI)
```

## Что такое Jetpack Benchmark?

**Jetpack Benchmark** — это библиотека от Google для точного измерения производительности Android приложений. Включает два типа тестов:

### 1. Microbenchmark (модуль `benchmark`)
- Измеряет производительность отдельных функций и алгоритмов
- Работает внутри процесса приложения
- Подходит для unit-тестов производительности

### 2. Macrobenchmark (модуль `macrobenchmark`)
- Измеряет производительность UI и пользовательских сценариев
- Работает в отдельном процессе
- Измеряет startup time, scroll performance, анимации

## Примеры бенчмарков

### Microbenchmark Example
```kotlin
@Test
fun stringConcatenation() = benchmarkRule.measureRepeated {
    var result = ""
    repeat(100) {
        result += "item$it"
    }
}
```

### Macrobenchmark Example
```kotlin
@Test
fun startup() = benchmarkRule.measureRepeated(
    packageName = "com.example.benchmarkdemo",
    metrics = listOf(StartupTimingMetric()),
    iterations = 5,
    startupMode = StartupMode.COLD
) {
    pressHome()
    startActivityAndWait()
}
```

## Как запустить бенчмарки

### Требования
- Android SDK с API 24+
- Подключенное устройство или эмулятор
- Release конфигурация для точных измерений

### Команды запуска

#### 🚀 Автоматический запуск с извлечением результатов:
```bash
# Запуск всех бенчмарков + автоматическое извлечение trace файлов
./run-benchmarks.sh

# Только микробенчмарки
./run-benchmarks.sh micro

# Только макробенчмарки
./run-benchmarks.sh macro
```

#### 📁 Извлечение trace файлов:
```bash
# Автоматическое извлечение всех trace файлов с устройства
./extract-traces.sh
```

#### Ручной запуск:
```bash
# Микро-бенчмарки
./gradlew benchmark:connectedAndroidTest

# Макро-бенчмарки
./gradlew macrobenchmark:connectedAndroidTest

# Сборка приложения
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home ./gradlew app:assembleRelease --no-daemon
```

## Результаты бенчмарков

Результаты сохраняются в:
- `benchmark/build/outputs/androidTest-results/` (локальные отчеты)
- `macrobenchmark/build/outputs/androidTest-results/` (локальные отчеты)
- `benchmark-results/additional_test_output/` (извлеченные trace файлы)

### Формат результатов
- **JSON файлы** с детальной статистикой (медиана, min/max, все измерения)
- **Perfetto Traces** (.perfetto-trace) - для анализа в Android Studio Profiler
- **Method Traces** (.trace) - для детального анализа вызовов методов
- Консольный вывод с медианой, min/max значениями

### 🎯 Автоматизация
Скрипты автоматически:
- ✅ Проверяют подключение устройства/эмулятора
- ✅ Очищают старые результаты перед запуском
- ✅ Запускают все необходимые бенчмарки
- ✅ Извлекают trace файлы с устройства
- ✅ Показывают детальную статистику по размерам файлов
- ✅ Предоставляют инструкции по анализу результатов

## Ключевые особенности

### Microbenchmark:
- Автоматическое устранение JIT warm-up
- Статистически значимые измерения
- Метрики: время выполнения, аллокации памяти

### Macrobenchmark:
- Измерение в production окружении
- Профилирование системных метрик
- Startup timing, frame metrics, energy usage

## Настройки производительности

### В build.gradle.kts:
```kotlin
android {
    testInstrumentationRunner = "androidx.benchmark.junit4.AndroidBenchmarkRunner"
    testBuildType = "release"
}
```

### Экспериментальные опции:
```kotlin
experimentalProperties["android.experimental.self-instrumenting"] = true
```

## Анализ результатов

1. **Android Studio Profiler** - для trace файлов
2. **Systrace/Perfetto** - для системного анализа
3. **Встроенная статистика** - медиана, разброс, доверительный интервал

## Полезные ссылки

- [Jetpack Benchmark Guide](https://developer.android.com/topic/performance/benchmarking/overview)
- [Macrobenchmark Documentation](https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview)
- [Performance Best Practices](https://developer.android.com/topic/performance)

## Результаты выполнения

📊 **Подробные результаты смотрите в [BENCHMARK_RESULTS.md](./BENCHMARK_RESULTS.md)**

### Итоговые результаты:

#### ✅ Микробенчмарки (StringBenchmark) - УСПЕШНО!
- **Статус**: Успешно выполнены с подавлением ошибок эмулятора
- **Результаты**: StringBuilder в **7.6 раза быстрее** String concatenation
- **Данные**:
  - String concatenation: медиана 56,220 нс (min: 51,997, max: 84,730)
  - StringBuilder: медиана 7,353 нс (min: 7,006, max: 11,068)
  - joinToString(): медиана 11,075 нс (min: 10,510, max: 22,003)
- **Протестировано**: string concatenation, StringBuilder, joinToString()

#### ✅ Макробенчмарки (StartupBenchmark) - УСПЕШНО!
- **Статус**: Успешно выполнены для обоих билдов (benchmark + debug)
- **Измерения**: Холодный запуск приложения с StartupTimingMetric
- **Время**: Выполнение за 6 секунд

### Ключевые достижения:
1. **🎯 Получены объективные численные результаты** производительности
2. **📈 Подтверждены лучшие практики** - StringBuilder действительно быстрее в 7.6 раза
3. **🛠️ Настроена полная инфраструктура** многомодульного бенчмарк-проекта
4. **📊 Созданы Perfetto traces** для углубленного анализа (805MB данных)
5. **🚀 Полная автоматизация** - скрипты для запуска и извлечения результатов

### Настройка для эмулятора:
```kotlin
testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR,LOW-BATTERY"
```

## Примеры использования

В этом проекте демонстрируются:
- Сравнение производительности String vs StringBuilder (7.6x разница!)
- Измерение времени запуска приложения (cold/warm/hot)
- Профилирование списков и scroll performance
- Настройка многомодульного проекта для бенчмарков
- Интеграция с Android Profiler и Perfetto
- **Полная автоматизация** процесса измерений

### 🚀 Быстрый старт:
```bash
# Один скрипт запускает всё - от бенчмарков до извлечения trace файлов
./run-benchmarks.sh

# Результат: готовые для анализа файлы в benchmark-results/
```

### 📊 Что получите:
- **3 Perfetto Traces** (~100-200MB каждый) для Android Studio Profiler
- **3 Method Traces** (~80-140KB) для детального анализа вызовов
- **JSON со статистикой** - медиана, min/max, все измерения
- **Готовые инструкции** по анализу результатов

## 🛠️ Технические детали автоматизации

### extract-traces.sh
Автоматический скрипт для извлечения trace файлов:
- Проверяет подключение Android устройства/эмулятора
- Автоматически находит все benchmark файлы на устройстве
- Извлекает файлы через ADB с полным отчетом
- Показывает размеры файлов и инструкции по использованию
- Поддерживает цветной вывод для лучшей читаемости

### run-benchmarks.sh
Полный цикл тестирования одной командой:
- Очищает старые результаты перед запуском
- Поддерживает параметры: `all` (по умолчанию), `micro`, `macro`
- Автоматически настраивает JAVA_HOME для Android проектов
- Запускает бенчмарки с правильными флагами `--no-daemon`
- Интегрирует извлечение trace файлов после выполнения
- Предоставляет детальную диагностику при ошибках

### Что автоматизировано:
1. **Проверка окружения** - Android SDK, устройство, Java
2. **Очистка данных** - старые результаты и trace файлы
3. **Компиляция и запуск** - с правильными конфигурациями
4. **Извлечение результатов** - все файлы с устройства в локальную папку
5. **Анализ и отчеты** - размеры файлов, инструкции по использованию

### Практические преимущества:
- **Студенты** фокусируются на анализе результатов, а не на настройке инфраструктуры
- **Преподаватели** получают воспроизводимый процесс демонстрации
- **Разработчики** могут быстро получить benchmark данные для оптимизации