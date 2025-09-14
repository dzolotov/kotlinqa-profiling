# JVM Performance Monitoring Stack

Демонстрационное приложение для изучения профилирования JVM с помощью современного стека мониторинга.

## 🏗️ Архитектура

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Java App        │    │ Prometheus      │    │ Grafana         │
│ + JMX Agent     │───▶│ Time Series DB  │───▶│ Dashboards      │
│ :9404/metrics   │    │ :9090           │    │ :3000           │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🚀 Быстрый старт

### Запуск
```bash
./start.sh
```

### Проверка статуса
```bash
./check.sh
```

### Остановка
```bash
./stop.sh
```

## 📊 Дашборды Grafana

После запуска доступны два дашборда:

### 1. Полный дашборд (13 панелей)
**Рекомендуется для обучения**
- http://localhost:3000/d/jvm-complete/jvm-complete-monitoring

Включает:
- JVM Heap/Non-Heap Memory
- Memory Pools (используемая, выделенная, максимальная)
- JVM Threads (текущие, daemon, пиковые)
- Garbage Collection (частота и время)
- JVM Classes (загруженные, выгруженные)
- Heap Usage % (gauge)
- Current Thread Count (gauge)
- Статистика финализации объектов

### 2. Базовый дашборд (5 панелей)
**Для быстрого обзора**
- http://localhost:3000/d/jvm-monitoring/jvm-performance-monitoring

Включает основные метрики:
- Heap Memory
- Non-Heap Memory
- Memory Pools
- Threads
- GC Collections

## 🔧 Доступные инструменты

### Grafana
- URL: http://localhost:3000
- Логин: `admin` / Пароль: `admin`

### Prometheus
- URL: http://localhost:9090
- Targets: http://localhost:9090/targets
- Alerts: http://localhost:9090/alerts

### JMX Metrics
- URL: http://localhost:9404/metrics
- Формат: Prometheus metrics

## 🎯 Демо приложение

`PerformanceTestApp.kt` симулирует различные нагрузки:
- CPU-интенсивные вычисления (Fibonacci)
- Интенсивные аллокации памяти
- Многопоточность
- Memory leaks для тестирования GC

## 🔍 Поддерживаемые профайлеры

### 1. VisualVM
```bash
# Установка
brew install --cask visualvm

# Запуск
visualvm
```

### 2. async-profiler
```bash
# Скачивание
wget https://github.com/jvm-profiling-tools/async-profiler/releases/download/v2.9/async-profiler-2.9-macos.tar.gz
tar -xzf async-profiler-2.9-macos.tar.gz

# Использование
java -jar profiler.jar -d 30 -f profile.html <PID>
```

### 3. Java Flight Recorder (JFR)
```bash
# Запуск с JFR
java -XX:+FlightRecorder -XX:+UnlockCommercialFeatures -XX:StartFlightRecording=duration=60s,filename=recording.jfr PerformanceTestApp

# Анализ с JDK Mission Control
jmc
```

### 4. YourKit
```bash
# Агент для профилирования
java -agentpath:/Applications/YourKit-Java-Profiler-2023.9.app/Contents/Resources/bin/mac/libyjpagent.dylib PerformanceTestApp
```

## 🚀 Быстрый старт с Docker Compose

### Автоматический запуск (рекомендуется)
```bash
# Запуск всего стека одной командой
./start.sh

# Проверка статуса
./status.sh

# Остановка с опциями
./stop.sh
```

### Ручной запуск
```bash
# 1. Компиляция приложения
kotlinc PerformanceTestApp.kt -include-runtime -d PerformanceTestApp.jar

# 2. Запуск полного стека мониторинга
docker-compose up -d

# 3. Доступ к интерфейсам:
# - Grafana: http://localhost:3000 (admin/admin)
# - Prometheus: http://localhost:9090
# - JMX Metrics: http://localhost:9404/metrics
```

## 📜 Описание скриптов

### `start.sh`
- ✅ Проверяет наличие Docker, Docker Compose и Kotlin
- ✅ Автоматически компилирует JAR если нужно
- ✅ Запускает весь стек мониторинга
- ✅ Показывает ссылки на все сервисы
- ✅ Опционально открывает Grafana в браузере

### `status.sh`
- 📊 Показывает статус всех контейнеров
- 🔍 Проверяет доступность сервисов
- 📈 Отображает текущие JVM метрики
- 💾 Показывает использование ресурсов

### `stop.sh`
- 🛑 Интерактивное меню остановки
- 💾 Опция сохранения или удаления данных
- 🔄 Возможность перезапуска
- 📝 Просмотр логов перед остановкой

**Что запускается:**
- ✅ Java приложение с JMX Prometheus агентом (метрики на порту 9404)
- ✅ Prometheus сервер (порт 9090)
- ✅ Grafana с готовыми дашбордами (порт 3000)

## Локальный запуск (без Docker)

```bash
# Компиляция
kotlinc PerformanceTestApp.kt -include-runtime -d PerformanceTestApp.jar

# Запуск с JMX поддержкой
java -Dcom.sun.management.jmxremote \
     -Dcom.sun.management.jmxremote.port=9999 \
     -Dcom.sun.management.jmxremote.authenticate=false \
     -Dcom.sun.management.jmxremote.ssl=false \
     -jar PerformanceTestApp.jar

# Или напрямую
kotlin PerformanceTestApp.kt
```

## Сценарии тестирования

1. **CPU профилирование**: Наблюдение за вычислением чисел Фибоначчи
2. **Memory профилирование**: Отслеживание аллокаций и сборки мусора
3. **Threading профилирование**: Анализ многопоточного выполнения
4. **Allocation профилирование**: Мониторинг создания объектов

## Команды для различных профайлеров

### async-profiler команды
```bash
# CPU профилирование
java -jar profiler.jar -e cpu -d 30 -f cpu-profile.html <PID>

# Memory профилирование (аллокации)
java -jar profiler.jar -e alloc -d 30 -f memory-profile.html <PID>

# Профилирование блокировок
java -jar profiler.jar -e lock -d 30 -f lock-profile.html <PID>
```

### JFR команды
```bash
# Запись профиля
jcmd <PID> JFR.start duration=60s filename=recording.jfr

# Дамп профиля
jcmd <PID> JFR.dump filename=recording.jfr

# Остановка записи
jcmd <PID> JFR.stop
```

## Что искать в профилях

1. **Горячие методы** - методы, потребляющие больше всего CPU времени
2. **Утечки памяти** - объекты, которые не освобождаются GC
3. **Блокировки потоков** - долгие ожидания на synchronized блоках
4. **Частые аллокации** - места в коде с большим количеством создания объектов
5. **GC активность** - частота и длительность сборок мусора

## 📊 Мониторинг с Prometheus и Grafana

### Архитектура мониторинга
```
Java App + JMX Agent (9404) → Prometheus (9090) → Grafana (3000)
```

### Доступные метрики
- **Memory**: heap/non-heap usage, memory pools
- **Garbage Collection**: GC rate, pause times, collection count
- **Threads**: active, daemon, peak thread count
- **CPU**: process and system CPU load
- **Class Loading**: loaded/unloaded classes count
- **Compilation**: JIT compilation time

### Grafana дашборд "JVM Performance"
Автоматически настроенный дашборд включает:
- 📈 **Memory Usage** - использование heap/non-heap памяти
- 🗑️ **GC Rate** - частота сборок мусора по типам
- 🧵 **Thread Count** - активные, daemon и пиковые потоки
- ⚡ **CPU Load** - загрузка процесса и системы

### Команды Docker
```bash
# Просмотр логов
docker-compose logs -f performance-app

# Перезапуск только Java приложения
docker-compose restart performance-app

# Остановка всех сервисов
docker-compose down

# Очистка всех данных
docker-compose down -v

# Масштабирование (несколько инстансов)
docker-compose up -d --scale performance-app=3
```

### Анализ производительности
1. **Запустите мониторинг** через `./start.sh`
2. **Откройте Grafana** на http://localhost:3000
3. **Перейдите в дашборд** "JVM Performance Monitoring"
4. **Наблюдайте метрики** во время выполнения нагрузочных тестов
5. **Анализируйте паттерны** GC, memory leaks, CPU spikes

### Алерты и уведомления
Добавьте в `prometheus.yml` правила для алертинга:
- Memory usage > 80%
- GC frequency > 5/sec
- Thread count > 100
- CPU load > 90%