import kotlin.random.Random

/**
 * Демо-приложение для тестирования с различными профайлерами:
 * - VisualVM
 * - async-profiler
 * - Java Flight Recorder (JFR)
 * - YourKit
 */
class PerformanceTestApp {

    private val data = mutableListOf<String>()
    private val cache = mutableMapOf<Int, String>()

    fun runCpuIntensiveTask() {
        println("Запуск CPU-интенсивной задачи...")
        repeat(1000000) { i ->
            // Имитация сложных вычислений
            val result = calculateFibonacci(i % 30)
            if (i % 10000 == 0) {
                println("Обработано: $i, Fibonacci: $result")
            }
        }
    }

    private fun calculateFibonacci(n: Int): Long {
        return if (n <= 1) n.toLong()
        else calculateFibonacci(n - 1) + calculateFibonacci(n - 2)
    }

    fun runMemoryIntensiveTask() {
        println("Запуск memory-интенсивной задачи...")
        repeat(100000) { i ->
            // Создаем много объектов
            val randomString = generateRandomString(100)
            data.add(randomString)

            // Заполняем кеш
            cache[i] = randomString

            // Периодически очищаем для имитации утечек памяти
            if (i % 10000 == 0) {
                data.clear()
                System.gc() // Принудительный GC для наблюдения
                println("Очищено данных: $i, размер кеша: ${cache.size}")
            }
        }
    }

    private fun generateRandomString(length: Int): String {
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..length)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
    }

    fun runThreadingTask() {
        println("Запуск многопоточной задачи...")
        val threads = mutableListOf<Thread>()

        repeat(10) { threadId ->
            val thread = Thread {
                repeat(50000) { iteration ->
                    // Имитация работы с общими ресурсами
                    synchronized(this) {
                        val key = threadId * 1000 + iteration
                        cache[key] = "thread-$threadId-iter-$iteration"
                    }

                    // Имитация I/O или ожидания
                    if (iteration % 1000 == 0) {
                        Thread.sleep(1)
                    }
                }
                println("Завершен поток $threadId")
            }
            threads.add(thread)
            thread.start()
        }

        // Ждем завершения всех потоков
        threads.forEach { it.join() }
        println("Все потоки завершены")
    }

    fun runAllocationHeavyTask() {
        println("Запуск задачи с интенсивными аллокациями...")
        val lists = mutableListOf<MutableList<Any>>()

        repeat(1000) { round ->
            val list = mutableListOf<Any>()

            // Создаем много объектов разных типов
            repeat(1000) { i ->
                when (i % 5) {
                    0 -> list.add("String $i")
                    1 -> list.add(i)
                    2 -> list.add(i.toDouble())
                    3 -> list.add(listOf(i, i * 2, i * 3))
                    4 -> list.add(mapOf("key$i" to "value$i"))
                }
            }

            lists.add(list)

            // Периодически создаем давление на память
            if (round % 100 == 0) {
                lists.clear()
                System.gc()
                println("Раунд аллокаций: $round")
            }
        }
    }
}

fun main() {
    val app = PerformanceTestApp()

    println("=== Начинаем тестирование производительности ===")
    println("Подключите профайлер (VisualVM, async-profiler, JFR) к процессу ${ProcessHandle.current().pid()}")
    println("Нажмите Enter для продолжения...")
    readLine()

    // Запускаем разные типы нагрузки для профилирования
    app.runCpuIntensiveTask()
    Thread.sleep(2000)

    app.runMemoryIntensiveTask()
    Thread.sleep(2000)

    app.runThreadingTask()
    Thread.sleep(2000)

    app.runAllocationHeavyTask()

    println("=== Тестирование завершено ===")
    println("Проанализируйте результаты в профайлере")
}