# Baseline Profiles Examples

Примеры использования Baseline Profiles для улучшения производительности Android приложений на 30%.

## Что такое Baseline Profiles?

Baseline Profiles - это механизм Android для указания, какие части кода должны быть скомпилированы AOT (Ahead-of-Time) для улучшения производительности при запуске и работе приложения.

### Преимущества:
- Улучшение времени запуска на 30%
- Снижение jank-эффектов
- Оптимизация "горячих" путей выполнения
- Лучшая производительность на устройствах среднего класса

## Файлы

### BaselineProfileRule.kt
Правила для генерации Baseline Profile:
- Сценарии пользовательского поведения
- Навигация по приложению
- Тестирование различных use-cases

### build.gradle.kts
Конфигурация модуля для генерации профилей:
- Настройка плагина `androidx.baselineprofile`
- Конфигурация управляемых устройств
- Зависимости для UI тестирования

### baseline-prof.txt
Пример сгенерированного профиля со списком "горячих" методов и классов

## Настройка проекта

### 1. Структура модулей
```
app/
├── build.gradle.kts (основной модуль)
└── src/main/...

baselineprofile/
├── build.gradle.kts (модуль для генерации профилей)
└── src/main/java/...
```

### 2. Настройка основного модуля (app/build.gradle.kts)
```kotlin
plugins {
    id("com.android.application")
    id("androidx.baselineprofile")
}

android {
    buildTypes {
        create("benchmark") {
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
            isProfileable = true // Критически важно!
        }
    }
}

dependencies {
    baselineProfile(project(":baselineprofile"))
}
```

### 3. Настройка модуля профилирования (baselineprofile/build.gradle.kts)
```kotlin
plugins {
    id("com.android.test")
    id("androidx.baselineprofile")
}

android {
    targetProjectPath = ":app"

    testOptions {
        managedDevices {
            devices {
                create<ManagedVirtualDevice>("pixel6Api33") {
                    device = "Pixel 6"
                    apiLevel = 33
                    systemImageSource = "aosp"
                }
            }
        }
    }
}

baselineProfile {
    managedDevices += "pixel6Api33"
    useConnectedDevices = true
}
```

### 4. Настройка на уровне проекта (settings.gradle.kts)
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

include(":app")
include(":baselineprofile")
```

## Генерация профилей

### Через управляемые устройства
```bash
# Генерация на управляемом устройстве
./gradlew :baselineprofile:pixel6Api33BaselineProfileBenchmark

# Применение профилей к основному модулю
./gradlew :baselineprofile:copyBaseline
```

### Через подключенное устройство
```bash
# Генерация на подключенном устройстве/эмуляторе
./gradlew :baselineprofile:connectedBaselineProfileAndroidTest
```

## Проверка результатов

### 1. Расположение сгенерированных файлов
```
app/src/main/baseline-prof.txt
```

### 2. Проверка применения профилей
```bash
# Сборка с Baseline Profiles
./gradlew :app:assembleBenchmark

# Анализ размера APK
./gradlew :app:analyzeBenchmarkBundle
```

### 3. Измерение производительности
```kotlin
@Test
fun startupBenchmark() = benchmarkRule.measureRepeated(
    packageName = "com.example.myapp",
    metrics = listOf(StartupTimingMetric()),
    iterations = 5,
    startupMode = StartupMode.COLD
) {
    pressHome()
    startActivityAndWait()
}
```

## Лучшие практики

### Сценарии для профилирования
1. **Холодный старт**: Полный запуск приложения
2. **Навигация**: Переходы между основными экранами
3. **Списки**: Прокрутка больших списков данных
4. **Поиск**: Часто используемые функции поиска
5. **Загрузка данных**: Сетевые запросы и кэширование

### Оптимизация профилей
- Фокусируйтесь на часто используемых путях
- Включайте инициализацию критических компонентов
- Тестируйте различные пользовательские сценарии
- Регулярно обновляйте профили при изменении архитектуры

### Мониторинг эффективности
```bash
# Сравнение производительности с/без профилей
./gradlew :app:connectedBenchmarkAndroidTest

# Анализ метрик запуска
adb shell am start -W com.example.myapp/.MainActivity
```

## Интеграция в CI/CD

```yaml
name: Generate Baseline Profile
on:
  pull_request:
    paths: ['app/**', 'baselineprofile/**']

jobs:
  baseline-profile:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Setup Android SDK
        uses: android-actions/setup-android@v2

      - name: Generate Baseline Profile
        run: |
          ./gradlew :baselineprofile:pixel6Api33BaselineProfileBenchmark
          ./gradlew :baselineprofile:copyBaseline

      - name: Commit updated profile
        run: |
          git add app/src/main/baseline-prof.txt
          git commit -m "Update baseline profile" || exit 0
```

Baseline Profiles - это мощный инструмент для улучшения производительности Android приложений с минимальными усилиями разработчика.