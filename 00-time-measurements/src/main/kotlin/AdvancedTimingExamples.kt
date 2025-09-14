import kotlin.system.measureTimeMillis
import kotlin.system.measureNanoTime

/**
 * Продвинутые примеры измерения времени
 * Демонстрирует тонкости и подводные камни
 */
object AdvancedTimingExamples {

    @JvmStatic
    fun main(args: Array<String>) {
        println("🎯 Продвинутые техники измерения времени")
        println("=" * 50)

        demonstrateWarmupImportance()
        println()
        demonstrateGCInterference()
        println()
        demonstrateResolutionLimits()
        println()
        demonstrateStatisticalApproach()
        println()
        showRealWorldScenarios()
    }

    /**
     * Демонстрация важности прогрева JVM
     */
    private fun demonstrateWarmupImportance() {
        println("🔥 Важность прогрева JVM")
        println("-".repeat(30))

        val operation = {
            // Операция с классами и методами, которые JVM нужно оптимизировать
            val list = mutableListOf<Double>()
            repeat(10_000) {
                list.add(Math.sqrt(it.toDouble()))
                if (it % 1000 == 0) {
                    list.clear()
                    list.add(Math.sin(it.toDouble()))
                }
            }
            list.size
        }

        println("❄️ БЕЗ прогрева (холодный старт):")
        repeat(5) { iteration ->
            val time = measureNanoTime { operation() }
            println("  Попытка ${iteration + 1}: ${String.format("%.2f", time/1_000_000.0)}мс")
        }

        println("\n🔥 ПРОГРЕВ (100 итераций)...")
        repeat(100) { operation() }

        println("\n🚀 ПОСЛЕ прогрева:")
        repeat(5) { iteration ->
            val time = measureNanoTime { operation() }
            println("  Попытка ${iteration + 1}: ${String.format("%.2f", time/1_000_000.0)}мс")
        }

        println("\n💡 Заметьте разницу! JVM оптимизировал код после прогрева.")
    }

    /**
     * Демонстрация влияния Garbage Collector
     */
    private fun demonstrateGCInterference() {
        println("🗑️ Влияние Garbage Collector")
        println("-".repeat(30))

        println("Выполняем операцию, создающую много объектов...")

        val measurements = mutableListOf<Long>()

        repeat(20) { iteration ->
            val time = measureNanoTime {
                // Операция, создающая много мусора
                val bigList = mutableListOf<String>()
                repeat(50_000) {
                    bigList.add("Строка номер $it с дополнительным текстом для создания объектов")
                    if (it % 10_000 == 0) {
                        // Создаем еще больше объектов
                        val tempList = bigList.map { s -> s.uppercase() }
                    }
                }
                bigList.size
            }

            measurements.add(time)
            val timeMs = time / 1_000_000.0
            val marker = if (timeMs > measurements.take(5).average() / 1_000_000.0 * 2) " 🗑️ GC?" else ""
            println("  Измерение ${String.format("%2d", iteration + 1)}: ${String.format("%6.2f", timeMs)}мс$marker")
        }

        val avg = measurements.average() / 1_000_000.0
        val outliers = measurements.filter { it / 1_000_000.0 > avg * 1.5 }

        println("\n📊 Анализ:")
        println("  Среднее время: ${String.format("%.2f", avg)}мс")
        println("  Выбросы (>150% среднего): ${outliers.size}")
        println("  💡 Выбросы обычно связаны с работой GC")
    }

    /**
     * Демонстрация ограничений разрешения времени
     */
    private fun demonstrateResolutionLimits() {
        println("📏 Ограничения разрешения времени")
        println("-".repeat(30))

        println("Тестируем очень быструю операцию:")

        // Очень быстрая операция
        val fastOperation = {
            var sum = 0
            for (i in 1..100) {
                sum += i
            }
            sum
        }

        // Измеряем разными способами
        println("\nmeasureTimeMillis (разрешение ~1мс):")
        repeat(10) { i ->
            val time = measureTimeMillis { fastOperation() }
            print("$time ")
        }
        println("мс")

        println("\nmeasureNanoTime (разрешение ~наносекунды):")
        repeat(10) { i ->
            val time = measureNanoTime { fastOperation() }
            print("${time} ")
        }
        println("нс")

        println("\n🔍 Исследуем реальное разрешение System.nanoTime():")
        val resolution = measureTimeResolution()
        println("  Минимальное измеримое время: ${resolution}нс")

        if (resolution > 1000) {
            println("  ⚠️ Разрешение хуже 1мкс - возможны неточности!")
        } else {
            println("  ✅ Хорошее разрешение для микробенчмарков")
        }
    }

    /**
     * Измерение реального разрешения System.nanoTime()
     */
    private fun measureTimeResolution(): Long {
        var minDiff = Long.MAX_VALUE
        var prevTime = System.nanoTime()

        repeat(1000) {
            val currentTime = System.nanoTime()
            val diff = currentTime - prevTime
            if (diff > 0 && diff < minDiff) {
                minDiff = diff
            }
            prevTime = currentTime
        }

        return minDiff
    }

    /**
     * Статистический подход к измерениям
     */
    private fun demonstrateStatisticalApproach() {
        println("📈 Статистический подход")
        println("-".repeat(30))

        val testOperation = {
            // Операция средней сложности с некоторой вариативностью
            val random = kotlin.random.Random
            repeat(random.nextInt(5000, 15000)) {
                val dummy = Math.sqrt(it.toDouble()) + Math.sin(it * random.nextDouble())
            }
        }

        println("Собираем статистику по 50 измерениям...")
        val measurements = mutableListOf<Double>()

        // Прогрев
        repeat(20) { testOperation() }

        // Сбор данных
        repeat(50) {
            val time = measureNanoTime { testOperation() } / 1_000_000.0
            measurements.add(time)
        }

        // Статистический анализ
        val sorted = measurements.sorted()
        val mean = measurements.average()
        val median = sorted[sorted.size / 2]
        val q1 = sorted[sorted.size / 4]
        val q3 = sorted[sorted.size * 3 / 4]
        val min = sorted.first()
        val max = sorted.last()
        val stdDev = kotlin.math.sqrt(measurements.map { (it - mean) * (it - mean) }.average())

        println("\n📊 Статистика (в мс):")
        println("  Среднее:     ${String.format("%6.3f", mean)}")
        println("  Медиана:     ${String.format("%6.3f", median)}")
        println("  Мин:         ${String.format("%6.3f", min)}")
        println("  Макс:        ${String.format("%6.3f", max)}")
        println("  Q1:          ${String.format("%6.3f", q1)}")
        println("  Q3:          ${String.format("%6.3f", q3)}")
        println("  Ст.откл:     ${String.format("%6.3f", stdDev)}")
        println("  Коэф.вар:    ${String.format("%6.1f", (stdDev/mean)*100)}%")

        // Выбросы
        val iqr = q3 - q1
        val lowerBound = q1 - 1.5 * iqr
        val upperBound = q3 + 1.5 * iqr
        val outliers = measurements.filter { it < lowerBound || it > upperBound }

        println("  Выбросы:     ${outliers.size} (${String.format("%.1f", outliers.size.toDouble()/measurements.size*100)}%)")

        if (stdDev / mean > 0.2) {
            println("  ⚠️ Высокая вариативность! Возможны помехи.")
        } else {
            println("  ✅ Приемлемая стабильность измерений.")
        }
    }

    /**
     * Реальные сценарии использования
     */
    private fun showRealWorldScenarios() {
        println("🌍 Реальные сценарии")
        println("-".repeat(30))

        println("1. 📁 Измерение времени чтения файла:")
        measureFileOperation()

        println("\n2. 🔄 Измерение времени парсинга JSON:")
        measureJSONParsing()

        println("\n3. 🧮 Измерение алгоритмических операций:")
        measureAlgorithmPerformance()
    }

    private fun measureFileOperation() {
        // Симуляция чтения файла
        val time = measureTimeMillis {
            Thread.sleep(kotlin.random.Random.nextLong(10, 50)) // 10-50мс
            repeat(1000) {
                val content = "Симуляция содержимого файла строка $it"
                content.length // Обработка
            }
        }
        println("  Время 'чтения файла': ${time}мс")

        if (time < 5) {
            println("  💡 Для таких быстрых операций лучше использовать measureNanoTime")
        }
    }

    private fun measureJSONParsing() {
        // Симуляция парсинга JSON
        val jsonData = """{"users":[{"name":"User1","age":25},{"name":"User2","age":30}]}"""

        val time = measureNanoTime {
            // Простейший "парсинг"
            val userCount = jsonData.count { it == '{' } - 1 // -1 для основного объекта
            repeat(userCount * 1000) {
                val dummy = jsonData.hashCode() + it
            }
        }

        println("  Время 'парсинга JSON': ${String.format("%.3f", time/1_000_000.0)}мс")
    }

    private fun measureAlgorithmPerformance() {
        val data = (1..10000).toList().shuffled()

        // Измеряем сортировку
        val sortTime = measureNanoTime {
            data.sorted()
        }

        // Измеряем поиск
        val searchTime = measureNanoTime {
            data.find { it == 5000 }
        }

        println("  Сортировка 10k элементов: ${String.format("%.3f", sortTime/1_000_000.0)}мс")
        println("  Поиск элемента: ${String.format("%.3f", searchTime/1_000.0)}мкс")

        // Сравнение с бинарным поиском
        val sortedData = data.sorted()
        val binarySearchTime = measureNanoTime {
            sortedData.binarySearch(5000)
        }
        println("  Бинарный поиск: ${String.format("%.3f", binarySearchTime/1_000.0)}мкс")

        val speedup = searchTime.toDouble() / binarySearchTime
        println("  🚀 Бинарный поиск быстрее в ${String.format("%.1f", speedup)} раз")
    }
}

private operator fun String.times(n: Int): String = this.repeat(n)