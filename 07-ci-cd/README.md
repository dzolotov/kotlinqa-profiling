# 07. CI/CD для Performance Testing

## 📋 Обзор

Данный пример демонстрирует автоматизацию тестирования производительности в CI/CD pipeline с помощью различных систем непрерывной интеграции. Включает конфигурации для GitHub Actions, GitLab CI/CD, TeamCity и Jenkins, а также вспомогательные скрипты для анализа и управления baseline метриками.

## 🎯 Цели примера

1. **Автоматизация performance testing** в различных CI/CD системах
2. **Обнаружение регрессий производительности** с помощью baseline сравнений
3. **Мониторинг трендов производительности** для Kotlin multiplatform проектов
4. **Интеграция с инструментами** профилирования (JMH, Jetpack Benchmark, kotlinx.benchmark)

## 🏗️ Структура проекта

```
07-ci-cd/
├── .github/
│   └── workflows/
│       └── performance-ci.yml           # GitHub Actions workflow
├── .gitlab-ci.yml                       # GitLab CI/CD конфигурация
├── teamcity-settings.xml               # TeamCity конфигурация
├── jenkins/
│   └── Jenkinsfile                      # Jenkins Pipeline
├── scripts/
│   ├── performance-comparison.py        # Скрипт сравнения производительности
│   └── baseline-updater.sh             # Обновление baseline метрик
├── test-data/                          # Тестовые данные
│   ├── baseline.json
│   ├── current-regression.json
│   └── current-improvement.json
└── baselines/                          # Baseline метрики
    └── js-baseline.json
```

## 🚀 CI/CD Workflows

### GitHub Actions (`github-workflows-performance-ci.yml`)

Комплексный workflow для автоматизации performance testing:

**Наиболее продвинутая конфигурация** с поддержкой:

#### 🎯 Возможности:
- **Матричное тестирование**: Ubuntu, macOS, Windows
- **Kotlin Multiplatform**: JVM, JS, Native таргеты
- **Android бенчмарки** с эмулятором
- **JMH детальный анализ**
- **Автоматическое извлечение** trace файлов
- **Performance regression detection**
- **Автоматические PR комментарии**
- **Еженедельные регрессионные тесты**

#### 🚀 Запуск:
```yaml
# Автоматически при:
- push в main/develop
- Pull Request в main
- Еженедельно (воскресенье, 2:00 UTC)
```

#### 📊 Результаты:
- **Artifacts**: результаты всех бенчмарков
- **HTML отчеты**: сводные результаты
- **GitHub Issues**: автоматические алерты при регрессии
- **PR комментарии**: результаты для код-ревью

---

### 2. **GitLab CI/CD** (`gitlab-ci.yml`)

#### 🎯 Особенности:
- **Пятиэтапный pipeline**: build → test-performance → report
- **Docker-based** окружение
- **GitLab Pages** интеграция для отчетов
- **Кеширование** Gradle для ускорения

#### 🏗️ Этапы:
1. **build**: компиляция проекта
2. **test-performance**: параллельные бенчмарки (JVM, JS, Native, Android, JMH)
3. **report**: сводный отчет и публикация

#### 📈 Интеграция:
- **GitLab Artifacts**: автоматическое сохранение результатов
- **Performance Reports**: встроенная поддержка GitLab
- **Pages**: публикация HTML отчетов

---

### 3. **TeamCity** (`teamcity-config.kts`)

#### 🎯 Преимущества:
- **Kotlin DSL**: типизированная конфигурация
- **Build Templates**: переиспользование настроек
- **Advanced Statistics**: детальная статистика
- **Agent Requirements**: точная настройка агентов

#### 🏗️ Build Types:
- `KotlinJvmBenchmarks`: Kotlin Multiplatform
- `AndroidPerformanceTests`: Android с эмулятором
- `JmhDeepAnalysis`: детальный JMH анализ
- `PerformanceReportAggregator`: сводные отчеты

#### 📊 Статистика:
- **Custom Metrics**: автоматический сбор метрик производительности
- **Build Comparisons**: сравнение между сборками
- **Performance Graphs**: графики трендов

---

### 4. **Jenkins** (`Jenkinsfile`)

#### 🎯 Особенности:
- **Declarative Pipeline**: читаемая конфигурация
- **Параллельное выполнение**: одновременные бенчмарки
- **Flexible Parameters**: настраиваемые запуски
- **HTML Reports**: встроенная поддержка отчетов

#### ⚙️ Параметры:
- `BENCHMARK_TYPE`: all/jvm-only/android-only/jmh-only
- `PERFORMANCE_STRICT_MODE`: строгий режим при регрессии
- `PERFORMANCE_THRESHOLD`: порог производительности

#### 🎨 Отчеты:
- **HTML Publisher**: публикация результатов
- **Test Results**: интеграция с Jenkins UI
- **Artifacts**: сохранение всех данных

---

## 🔧 Вспомогательные скрипты

### Performance Comparison (`performance-comparison.py`)

Анализирует результаты бенчмарков и выявляет регрессии:

```bash
python3 scripts/performance-comparison.py \
  baseline.json current-results.json \
  --format text \
  --warning-threshold 10 \
  --critical-threshold 30
```

#### ✨ Возможности:
- **Автоматическое обнаружение** регрессий и улучшений
- **Настраиваемые пороги** warning/critical
- **Множественные форматы** вывода (text, json, markdown)
- **Детальная статистика** изменений производительности

#### 📊 Пример вывода:
```
Performance Comparison Report
========================================

Total benchmarks: 3
Critical regressions: 0
Warning regressions: 2
Improvements: 0
Minor changes: 1

WARNING REGRESSIONS:
------------------
StringBenchmark.stringBuilder: +20.0% change
StringBenchmark.stringConcatenation: +50.0% change
```

### Baseline Updater (`baseline-updater.sh`)

Управляет baseline метриками производительности:

```bash
./scripts/baseline-updater.sh \
  --threshold 5 \
  --baseline-dir baselines \
  current-results.json
```

#### 🎯 Функционал:
- **Автоматическое обновление** baseline при улучшениях ≥5%
- **Backup система** с ротацией старых версий
- **Dry-run режим** для preview изменений
- **Валидация JSON** и детальная отчетность
- **Метаданные tracking** обновлений

---

## 🧪 Тестирование

Все компоненты проверены с тестовыми данными:

### Performance Comparison Script:
```bash
# Обнаружение регрессий (20% и 50% замедление)
✅ StringBenchmark.stringBuilder: +20.0% change
✅ StringBenchmark.stringConcatenation: +50.0% change

# Обнаружение улучшений (10% ускорение)
✅ StringBenchmark.stringBuilder: -10.0% improvement
✅ StringBenchmark.stringConcatenation: -10.3% improvement
```

### Baseline Updater:
```bash
✅ Создание backup файлов с timestamp
✅ Обновление baseline при значительных улучшениях
✅ Добавление метаданных с временем обновления
✅ Dry-run mode для предварительного просмотра
```

### CI/CD Integration:
```bash
✅ JMH results parsing как в GitHub Actions
✅ Performance report generation
✅ Artifacts structure симуляция
```

---

## 📊 Типы Performance Testing

### 1. **Kotlin Multiplatform Benchmarks**
```bash
# JVM
./gradlew jvmBenchmark

# JavaScript
./gradlew jsBenchmark

# Native
./gradlew nativeBenchmark
```

**Измеряем**: алгоритмическую производительность на разных платформах

### 2. **Android Benchmarks**
```bash
# Microbenchmarks
./gradlew benchmark:connectedAndroidTest

# Macrobenchmarks
./gradlew macrobenchmark:connectedAndroidTest
```

**Измеряем**: производительность Android приложений, startup time, UI rendering

### 3. **JMH Deep Analysis**
```bash
./gradlew jmhJar
java -jar build/libs/jmh.jar
```

**Измеряем**: микросекундные операции, JVM optimizations, memory allocations

---

## 🎛️ Настройка Performance Thresholds

### Пороги производительности:

```yaml
env:
  PERFORMANCE_THRESHOLD: "120%"  # 20% замедление = WARNING
  ALERT_THRESHOLD: "150%"        # 50% замедление = CRITICAL
  BASELINE_UPDATE_THRESHOLD: "5%" # 5% улучшение = обновить baseline
```

### Стратегии реагирования:

| Изменение | Действие | Уведомление |
|-----------|----------|------------|
| < 5% улучшение | ✅ Обновить baseline | PR комментарий |
| 5-20% замедление | ⚠️ Warning | PR комментарий |
| 20-50% замедление | 🚨 Fail CI | PR комментарий + Slack |
| > 50% замедление | 💥 Critical Alert | GitHub Issue + Email |

---

## 🔧 Настройка для вашего проекта

### 1. **GitHub Actions**

Скопируйте `.github/workflows/performance-ci.yml` в ваш репозиторий:

```bash
mkdir -p .github/workflows
cp examples/07-ci-cd/.github/workflows/performance-ci.yml .github/workflows/
```

**Настройте секреты** в GitHub Settings:
- `GITHUB_TOKEN` (автоматически доступен)
- `SLACK_WEBHOOK_URL` (опционально, для уведомлений)

### 2. **GitLab CI/CD**

Переименуйте и скопируйте:
```bash
cp examples/07-ci-cd/gitlab-ci.yml .gitlab-ci.yml
```

**Настройте переменные** в GitLab CI/CD Settings:
- `PERFORMANCE_THRESHOLD`
- `ANDROID_HOME`
- `SLACK_WEBHOOK` (опционально)

### 3. **TeamCity**

1. Импортируйте `teamcity-config.kts` в TeamCity
2. Настройте агенты с необходимыми tools
3. Создайте параметры проекта для thresholds

### 4. **Jenkins**

1. Создайте Multibranch Pipeline
2. Укажите `Jenkinsfile` как Pipeline script
3. Настройте параметры в Pipeline configuration

---

## 📈 Анализ результатов

### Артефакты, которые создаются:

#### 🔥 **Perfetto Traces** (Android)
- **Размер**: ~100-200MB каждый
- **Назначение**: детальный анализ в Android Studio Profiler
- **Содержимое**: CPU usage, memory allocation, method calls

#### ⚡ **Method Traces** (Android)
- **Размер**: ~50-150KB каждый
- **Назначение**: анализ вызовов методов
- **Содержимое**: call stack, timing data

#### 📊 **JSON Results** (все типы)
- **Размер**: ~5-50KB каждый
- **Назначение**: программный анализ результатов
- **Содержимое**: медиана, min/max, все измерения

#### 📋 **HTML Reports**
- **Размер**: ~10-100KB каждый
- **Назначение**: человекочитаемые отчеты
- **Содержимое**: таблицы, графики, сравнения

### Как читать результаты:

#### **JMH Results Example:**
```json
{
  "benchmark": "StringConcatenationBenchmark.stringBuilder",
  "primaryMetric": {
    "score": 7352.95,
    "scoreError": 145.23,
    "scoreUnit": "ns/op"
  },
  "mode": "avgt"
}
```

**Интерпретация**: StringBuilder выполняется за 7,352 наносекунды ± 145 ns

#### **Android Benchmark JSON:**
```json
{
  "name": "stringConcatenation",
  "metrics": {
    "timeNs": {
      "median": 56220.22,
      "minimum": 51997.37,
      "maximum": 84730.17
    }
  }
}
```

**Интерпретация**: String concatenation медиана 56,220 ns (в 7.6 раз медленнее StringBuilder)

---

## 🚨 Troubleshooting

### Проблемы с Android бенчмарками:

#### **"EMULATOR" ошибки:**
```yaml
testInstrumentationRunnerArguments:
  androidx.benchmark.suppressErrors: "EMULATOR,LOW-BATTERY,UNLOCKED"
```

#### **Нет подключенного устройства:**
```bash
# Проверка
adb devices

# Запуск эмулятора
$ANDROID_HOME/emulator/emulator -avd MyAVD
```

### Проблемы с JMH:

#### **OutOfMemoryError:**
```bash
export JMH_OPTS="-Xmx4g -XX:MaxMetaspaceSize=1g"
```

#### **Нет результатов:**
```bash
# Проверьте наличие @Benchmark аннотаций
# Проверьте создание jmh.jar
ls -la build/libs/jmh.jar
```

### Проблемы с CI/CD:

#### **Timeout на агентах:**
- Увеличьте timeout в конфигурации
- Используйте агенты с большим количеством CPU/RAM
- Уменьшите количество итераций для CI

#### **Нестабильные результаты:**
- Увеличьте warmup итерации
- Используйте dedicated агенты для performance testing
- Настройте более широкие tolerance thresholds

---

## 🎓 Образовательная ценность

### Для студентов:
1. **Реальный опыт** настройки CI/CD для performance
2. **Сравнение** разных CI систем и их особенностей
3. **Практика** анализа performance регрессий
4. **Понимание** важности автоматизации тестирования

### Для DevOps инженеров:
1. **Готовые конфигурации** для внедрения
2. **Best practices** performance testing в CI/CD
3. **Мониторинг** performance трендов
4. **Алертинг** при критических изменениях

### Для разработчиков:
1. **Feedback loop** по производительности кода
2. **Автоматическое** обнаружение регрессий
3. **Данные** для принятия архитектурных решений
4. **Культура** performance-aware разработки

---

## 🚀 Расширенные возможности

### Integration с мониторингом:

#### **Prometheus + Grafana:**
```yaml
- name: Export to Prometheus
  run: |
    # Преобразование benchmark результатов в Prometheus metrics
    python3 scripts/export-to-prometheus.py
```

#### **InfluxDB + Chronograf:**
```yaml
- name: Store in InfluxDB
  run: |
    # Отправка time-series данных
    python3 scripts/influxdb-exporter.py
```

### Продвинутые алерты:

#### **Slack интеграция:**
```python
def send_performance_alert(regression_data):
    slack_webhook = os.environ['SLACK_WEBHOOK_URL']
    message = {
        "text": f"🚨 Performance Regression Detected",
        "attachments": [{
            "color": "danger",
            "fields": [
                {"title": "Regression", "value": f"{regression_data['percent']}%"},
                {"title": "Benchmark", "value": regression_data['benchmark']},
                {"title": "Branch", "value": os.environ['GITHUB_REF']}
            ]
        }]
    }
    requests.post(slack_webhook, json=message)
```

#### **Email уведомления:**
```bash
# Настройка SMTP в CI переменных
SMTP_SERVER: "smtp.company.com"
SMTP_USER: "ci@company.com"
ALERT_RECIPIENTS: "team@company.com,devops@company.com"
```

---

## 📚 Дополнительные ресурсы

### Документация по CI системам:
- [GitHub Actions](https://docs.github.com/en/actions)
- [GitLab CI/CD](https://docs.gitlab.com/ee/ci/)
- [TeamCity](https://www.jetbrains.com/help/teamcity/)
- [Jenkins Pipeline](https://www.jenkins.io/doc/book/pipeline/)

### Performance Testing:
- [Kotlin Benchmarks](https://github.com/Kotlin/kotlinx-benchmark)
- [JMH Samples](https://github.com/openjdk/jmh)
- [Android Benchmark](https://developer.android.com/topic/performance/benchmarking)

### Лучшие практики:
- [Performance Testing Best Practices](https://martinfowler.com/articles/practical-test-pyramid.html)
- [CI/CD Performance Monitoring](https://www.thoughtworks.com/radar/techniques/performance-testing-in-continuous-delivery)

---

## 🎯 Выводы

Этот пример демонстрирует **production-ready подход** к интеграции performance testing в CI/CD:

- ✅ **Множественные платформы** - тестируем везде где работает код
- ✅ **Автоматизированное** обнаружение регрессий
- ✅ **Гибкая настройка** порогов и уведомлений
- ✅ **Готовые конфигурации** для всех популярных CI систем
- ✅ **Подробная документация** для внедрения

**Результат**: команда получает непрерывный мониторинг производительности с минимальными усилиями по настройке!