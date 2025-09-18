import kotlin.system.measureTimeMillis
import kotlin.system.measureNanoTime

/**
 * Демонстрация различных методов измерения времени в Kotlin
 *
 * Показывает проблемы и правильные подходы к измерению производительности
 */

object TimeMeasurementsDemo {

    @JvmStatic
    fun main(args: Array<String>) {

        println("🕒 Демонстрация измерения времени в Kotlin")
        println("=".repeat(50))

        demonstrateMeasureTimeMillis()
        println()
        demonstrateMeasureNanoTime()
        println()
        demonstrateMonotonicTimeProblem()
        println()
        demonstrateSystemTimeProblem()
        println()
        showCorrectMeasurementTechniques()
        println()
        compareDifferentApproaches()
    }

    /**
     * Демонстрация measureTimeMillis
     * ⚠️ ВАЖНО: measureTimeMillis использует System.currentTimeMillis() - НЕмонотонное время!
     * Может давать неверные результаты при NTP синхронизации или изменении системного времени
     * Хорошо для измерения операций длительностью > 1мс (но лучше использовать measureNanoTime)
     */
    private fun demonstrateMeasureTimeMillis() {
        println("📏 measureTimeMillis - для операций > 1мс")
        println("⚠️ ВНИМАНИЕ: использует System.currentTimeMillis() - немонотонное время!")
        println("-".repeat(40))

        // Быстрая операция - плохой кейс для measureTimeMillis
        val fastOperationTime = measureTimeMillis {
            var sum = 0L // Аккумулятор для предотвращения dead code elimination
            repeat(1000) {
                sum += it * it + it
            }
            sum // Используем результат
        }
        println("⚠️ Быстрая операция: ${fastOperationTime}мс (может показать 0мс!)")

        // Медленная операция - хороший кейс
        val slowOperationTime = measureTimeMillis {
            Thread.sleep(50) // 50мс
            var sum = 0.0
            repeat(100_000) {
                val dummy = Math.sqrt(it.toDouble())
                sum+=dummy
            }
            println(sum)
        }
        println("✅ Медленная операция: ${slowOperationTime}мс")

        // Демонстрация нестабильности для быстрых операций
        println("\n🔄 10 измерений быстрой операции:")
        repeat(10) { run ->
            val time = measureTimeMillis {
                var sum = 0L // Аккумулятор для предотвращения dead code elimination
                repeat(1000) {
                    sum += it * it * it
                }
                sum // Используем результат
            }
            print("$time ")
        }
        println("мс")
        println("👀 Обратите внимание на много нулей и нестабильность!")
    }

    /**
     * Демонстрация measureNanoTime
     * Подходит для быстрых операций
     */
    private fun demonstrateMeasureNanoTime() {
        println("⚡ measureNanoTime - для быстрых операций")
        println("-".repeat(40))

        // Быстрая операция - теперь видим результат
        val fastOperationNano = measureNanoTime {
            var sum = 0L // Аккумулятор для предотвращения dead code elimination
            repeat(1000) {
                sum += it * it + it
            }
            sum // Используем результат
        }
        println("✅ Быстрая операция: ${fastOperationNano}нс = ${fastOperationNano/1_000_000.0}мс")

        // Сравнение точности
        println("\n📊 Сравнение точности для быстрой операции:")
        println("measureTimeMillis vs measureNanoTime")

        repeat(5) { run ->
            val millis = measureTimeMillis {
                var sum = 0L
                repeat(500) {
                    sum += it * it + it / 2
                }
                sum
            }

            val nanos = measureNanoTime {
                var sum = 0L
                repeat(500) {
                    sum += it * it + it / 2
                }
                sum
            }

            println("Попытка ${run + 1}: ${millis}мс vs ${nanos}нс (${String.format("%.3f", nanos/1_000_000.0)}мс)")
        }
    }

    /**
     * Демонстрация проблемы с монотонным временем
     * System.currentTimeMillis() может "прыгать" при изменении системного времени
     */
    private fun demonstrateMonotonicTimeProblem() {
        println("⏰ Проблема монотонного времени")
        println("-".repeat(40))

        println("❌ НЕПРАВИЛЬНО: System.currentTimeMillis()")
        println("Проблемы:")
        println("1. Может прыгать при синхронизации времени")
        println("2. Может идти назад при изменении системного времени")
        println("3. Зависит от часового пояса и перехода на летнее время")

        // Демонстрация неправильного измерения
        val wrongMeasurement = demonstrateWrongTimeMeasurement()
        println("Результат неправильного измерения: ${wrongMeasurement}мс")

        println("\n✅ ПРАВИЛЬНО: System.nanoTime()")
        println("Преимущества:")
        println("1. Монотонно возрастающее время")
        println("2. Не зависит от системного времени")
        println("3. Высокая точность (наносекунды)")

        // Демонстрация правильного измерения
        val correctMeasurement = demonstrateCorrectTimeMeasurement()
        println("Результат правильного измерения: ${correctMeasurement/1_000_000.0}мс")
    }

    /**
     * Неправильное измерение времени через System.currentTimeMillis()
     */
    private fun demonstrateWrongTimeMeasurement(): Long {
        val startTime = System.currentTimeMillis()

        // Некоторая работа
        var sum = 0.0 // Аккумулятор для предотвращения dead code elimination
        repeat(10_000) {
            sum += Math.sqrt(it.toDouble()) + Math.sin(it.toDouble())
        }
        // sum используется неявно для предотвращения оптимизации

        val endTime = System.currentTimeMillis()
        return endTime - startTime
    }

    /**
     * Правильное измерение времени через System.nanoTime()
     */
    private fun demonstrateCorrectTimeMeasurement(): Long {
        val startTime = System.nanoTime()

        // Та же работа
        var sum = 0.0 // Аккумулятор для предотвращения dead code elimination
        repeat(10_000) {
            sum += Math.sqrt(it.toDouble()) + Math.sin(it.toDouble())
        }
        // sum используется неявно для предотвращения оптимизации

        val endTime = System.nanoTime()
        return endTime - startTime
    }

    /**
     * Демонстрация проблем с системным временем
     */
    private fun demonstrateSystemTimeProblem() {
        println("🚨 Проблемы системного времени")
        println("-".repeat(40))

        println("Примеры когда System.currentTimeMillis() может давать неверные результаты:")
        println("1. 🌐 NTP синхронизация - время может \"прыгнуть\" на несколько секунд")
        println("2. ⏰ Переход на летнее время - час может повториться или пропуститься")
        println("3. 🔧 Ручное изменение времени пользователем")
        println("4. 🔄 Корректировка часов операционной системой")

        // Симуляция проблемы (в реальности это может происходить)
        println("\n💡 Пример потенциальной проблемы:")
        simulateTimeJumpProblem()
    }

    /**
     * Симуляция проблемы с прыжком времени
     */
    private fun simulateTimeJumpProblem() {
        println("Представим что во время измерения произошла синхронизация NTP...")

        val measurements = mutableListOf<Long>()

        repeat(10) {
            val time = measureTimeMillis {
                Thread.sleep(10) // Короткая задержка

                // Симулируем случайный "прыжок" времени
                if (it == 5) {
                    println("  💥 [Симуляция] В этот момент NTP скорректировал время на -2 сек!")
                    // В реальности здесь System.currentTimeMillis() может дать отрицательный результат
                }

                var volatileSum = 0L
                repeat(1000) { j ->
                    volatileSum = (j * j + j).toLong() // Предотвращает оптимизацию
                }
            }
            measurements.add(time)
            println("  Измерение ${it + 1}: ${time}мс")
        }

        println("\n📊 Результаты измерений: ${measurements}")
        println("👀 Видите проблему? В реальности одно из измерений могло бы быть отрицательным!")
    }

    /**
     * Правильные техники измерения времени
     */
    private fun showCorrectMeasurementTechniques() {
        println("✅ Правильные техники измерения")
        println("-".repeat(40))

        println("1. ⚠️ measureTimeMillis использует НЕМОНОТОННОЕ время (System.currentTimeMillis)!")
        println("   Может давать неверные результаты при NTP синхронизации")
        println("2. ⚡ ПРЕДПОЧИТАЙТЕ measureNanoTime - использует монотонное время!")
        println("3. 🔄 Делайте множественные измерения и усредняйте")
        println("4. 🔥 Прогревайте JVM перед измерениями")

        println("\n📏 Демонстрация правильного подхода:")

        // Прогрев JVM
        println("🔥 Прогреваем JVM...")
        repeat(1000) {
            performTestOperation()
        }

        // Множественные измерения
        println("\n📊 Выполняем множественные измерения:")
        val measurements = mutableListOf<Long>()

        repeat(10) {
            val time = measureNanoTime {
                performTestOperation()
            }
            measurements.add(time)
            println("  Измерение ${it + 1}: ${String.format("%.3f", time/1_000_000.0)}мс")
        }

        // Статистика
        val avgTime = measurements.average()
        val minTime = measurements.minOrNull() ?: 0
        val maxTime = measurements.maxOrNull() ?: 0

        println("\n📈 Статистика:")
        println("  Среднее: ${String.format("%.3f", avgTime/1_000_000.0)}мс")
        println("  Минимум: ${String.format("%.3f", minTime/1_000_000.0)}мс")
        println("  Максимум: ${String.format("%.3f", maxTime/1_000_000.0)}мс")
        println("  Разброс: ${String.format("%.3f", (maxTime-minTime)/1_000_000.0)}мс")
    }

    /**
     * Тестовая операция для измерений
     */
    private fun performTestOperation() {
        var sum = 0.0 // Аккумулятор для предотвращения dead code elimination
        repeat(5000) {
            sum += it * it + it / 2 + Math.sqrt(it.toDouble())
        }
        // Используем результат чтобы предотвратить оптимизацию
        if (sum < 0) println("Unexpected negative sum")
    }

    /**
     * Сравнение разных подходов к измерению времени
     */
    private fun compareDifferentApproaches() {
        println("⚖️ Сравнение подходов")
        println("-".repeat(40))

        println("Тестируем операцию средней сложности...")
        val testOperation = {
            var sum = 0.0 // Аккумулятор для предотвращения dead code elimination
            repeat(50_000) {
                sum += Math.sin(it.toDouble()) + Math.cos(it.toDouble())
            }
            sum // Возвращаем результат
        }

        // 1. measureTimeMillis
        val millisTime = measureTimeMillis { testOperation() }

        // 2. measureNanoTime
        val nanoTime = measureNanoTime { testOperation() }

        // 3. Ручное измерение через System.nanoTime()
        val manualStart = System.nanoTime()
        testOperation()
        val manualEnd = System.nanoTime()
        val manualTime = manualEnd - manualStart

        println("📊 Результаты:")
        println("  measureTimeMillis: ${millisTime}мс")
        println("  measureNanoTime:   ${String.format("%.3f", nanoTime/1_000_000.0)}мс (${nanoTime}нс)")
        println("  Ручное измерение:  ${String.format("%.3f", manualTime/1_000_000.0)}мс (${manualTime}нс)")

        println("\n🎯 Рекомендации:")
        println("  • Для быстрых операций (<1мс): measureNanoTime (монотонное время)")
        println("  • Для медленных операций (>1мс): measureNanoTime (НЕ measureTimeMillis!)")
        println("  • ⚠️ measureTimeMillis использует немонотонное время - избегайте!")
        println("  • Для производственного кода: множественные измерения + статистика")
        println("  • Для бенчмарков: используйте JMH (Java Microbenchmark Harness)")
    }
}

/**
 * Расширение для удобства
 */
private operator fun String.times(n: Int): String = this.repeat(n)