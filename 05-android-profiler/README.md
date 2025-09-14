# Android Profiler Examples

Примеры кода для демонстрации возможностей Android Profiler и системных инструментов профилирования.

## Файлы

### ProfiledActivity.kt
Демонстрационное Android Activity с различными типами нагрузки:
- CPU-интенсивные операции
- Операции с памятью и GC активность
- Асинхронные операции с IO
- Использование системных трейсов (`Trace.beginSection`/`Trace.endSection`)

### CustomProfiler.kt
Кастомный профилировщик для измерения времени выполнения методов:
- Измерение времени выполнения
- Сбор статистики по вызовам
- Thread-safe реализация
- Удобные extension функции

### build.gradle.kts
Конфигурация проекта с поддержкой профилирования:
- Включение `isProfileable = true` для всех сборок
- Специальная `benchmark` конфигурация
- Зависимости для трейсинга

### Созданные скрипты
- **test-app.sh** - автоматическое тестирование приложения с симуляцией нагрузки
- **capture-trace.sh** - захват системного трейса для анализа производительности

## Использование

### 0. Быстрый старт
```bash
# Настроим Android SDK
export ANDROID_HOME="/Volumes/T7Shield/AndroidSdk"

# Запустим эмулятор (если еще не запущен)
$ANDROID_HOME/emulator/emulator -avd Otus_Emulator -no-window -no-audio &

# Соберем и установим приложение
JAVA_HOME=/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home ./gradlew installDebug --no-daemon

# Протестируем приложение
./test-app.sh

# Захватим системный трейс
./capture-trace.sh
```

### 1. Android Studio Profiler
1. Запустите приложение на устройстве или эмуляторе
2. Откройте View → Tool Windows → Profiler
3. Выберите ваше приложение и процесс
4. Используйте CPU, Memory, Network и Energy профилировщики

### 2. Системные трейсы
```bash
# Записать системный трейс
adb shell atrace -t 10 -b 32768 -o /data/local/tmp/trace.html view input sched freq

# Скачать трейс на компьютер
adb pull /data/local/tmp/trace.html

# Открыть в браузере Chrome: chrome://tracing
```

### 3. Method Tracing
```bash
# Использование команды am для запуска с трейсингом
adb shell am start -n com.example.profiler/.ProfiledActivity --start-profiler /data/local/tmp/method_trace.trace
```

### 4. Heap Dumps
```bash
# Создать heap dump
adb shell am dumpheap <PID> /data/local/tmp/heap.hprof

# Скачать на компьютер
adb pull /data/local/tmp/heap.hprof

# Конвертировать в стандартный формат (для некоторых инструментов)
hprof-conv heap.hprof heap-converted.hprof
```

### 5. Анализ HPROF файлов
Для анализа heap dumps можно использовать:

#### Android Studio Memory Profiler (рекомендуется)
```bash
# Конвертируем Android HPROF в стандартный формат
hprof-conv heap-dump.hprof heap-dump-converted.hprof

# Открываем в Android Studio: File → Open → выбираем .hprof файл
```

#### Eclipse Memory Analyzer (MAT) - самый популярный
```bash
# Скачать: https://eclipse.dev/mat/
# Или через brew:
brew install --cask mat

# Запустить и открыть .hprof файл
```

#### VisualVM - встроен в JDK
```bash
# Обычно уже установлен с JDK
visualvm

# File → Load → выбираем .hprof файл
```

#### IntelliJ IDEA (Ultimate Edition)
```bash
# Tools → Analyze Memory Snapshot
# Или просто перетащить .hprof файл в IDEA
```

**Что можно анализировать:**
- Memory leaks (утечки памяти)
- Object retention (какие объекты удерживают память)
- Heap composition (состав кучи по типам объектов)
- GC roots (корневые объекты для сборщика мусора)
- Duplicate strings (дублированные строки)
- Large objects (самые большие объекты)

## Полезные команды ADB

```bash
# Получить информацию о процессе
adb shell ps | grep com.example.profiler

# Мониторинг использования памяти
adb shell dumpsys meminfo com.example.profiler

# Анализ производительности GPU
adb shell dumpsys gfxinfo com.example.profiler

# Мониторинг батареи
adb shell dumpsys batterystats com.example.profiler
```

## Настройки для профилирования

### Подключение устройства
```bash
# Включить отладку по USB
# Включить GPU профилировщик в Developer Options
# Отключить анимации для точных измерений:
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
```

### Оптимальные настройки
- Использовать физическое устройство вместо эмулятора
- Закрыть другие приложения для минимизации шума
- Использовать Release сборки для production анализа
- Включить `isProfileable = true` в build.gradle