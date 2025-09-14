package com.example.profiler

import android.os.Bundle
import android.os.Debug
import android.os.Trace
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class ProfiledActivity : AppCompatActivity() {
    private lateinit var logTextView: TextView
    private val profiler = CustomProfiler.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Пример использования системных трейсов
        Trace.beginSection("onCreate")

        setContentView(R.layout.activity_main)

        logTextView = findViewById(R.id.tv_log)

        findViewById<Button>(R.id.btn_cpu_test).setOnClickListener {
            logMessage("Запускаем CPU тест...")
            performCpuIntensiveWork()
        }

        findViewById<Button>(R.id.btn_memory_test).setOnClickListener {
            logMessage("Запускаем Memory тест...")
            performMemoryIntensiveWork()
        }

        findViewById<Button>(R.id.btn_async_test).setOnClickListener {
            logMessage("Запускаем Async тест...")
            performAsyncWork()
        }

        findViewById<Button>(R.id.btn_show_stats).setOnClickListener {
            showProfilerStats()
        }

        // Включаем отладку памяти для детального анализа
        Debug.startMethodTracing("ProfiledActivity")

        logMessage("Activity создана. Готова к профилированию!")

        Trace.endSection()
    }

    private fun logMessage(message: String) {
        runOnUiThread {
            val currentText = logTextView.text.toString()
            val newText = if (currentText == "Performance logs will appear here...") {
                message
            } else {
                "$currentText\n$message"
            }
            logTextView.text = newText
        }
    }

    private fun showProfilerStats() {
        profiler.logStatistics()
        val stats = profiler.getMethodStatistics()
        var message = "=== Статистика профилировщика ===\n"
        stats.values.sortedByDescending { it.totalTimeMs }.forEach { stat ->
            message += "${stat.methodName}: ${stat.totalTimeMs}ms (${stat.callCount} вызовов)\n"
        }
        logMessage(message)
    }

    private fun performCpuIntensiveWork() {
        profiler.measureTime("performCpuIntensiveWork") {
            Trace.beginSection("CPU_Intensive_Work")

            // Имитация сложных вычислений
            repeat(5000) { i ->
                val fibonacci = calculateFibonacci(i % 25)
                // Создаем некоторую нагрузку на CPU
                val result = Math.pow(fibonacci.toDouble(), 2.0)

                if (i % 1000 == 0) {
                    logMessage("CPU test progress: ${i}/5000")
                }
            }

            Trace.endSection()
            logMessage("CPU тест завершен!")
        }
    }

    private fun performMemoryIntensiveWork() {
        profiler.measureTime("performMemoryIntensiveWork") {
            Trace.beginSection("Memory_Intensive_Work")

            // Создание больших объектов для анализа памяти
            val largeList = mutableListOf<ByteArray>()
            var totalAllocated = 0L

            repeat(50) { i ->
                // Создаем массивы разного размера для демонстрации allocation patterns
                val size = (1024 * (i + 1)) // 1KB, 2KB, 3KB...
                val byteArray = ByteArray(size) { Random.nextInt().toByte() }
                largeList.add(byteArray)
                totalAllocated += size

                if (i % 10 == 0) {
                    logMessage("Memory allocated: ${totalAllocated / 1024}KB")
                }

                // Периодически очищаем для демонстрации GC активности
                if (i % 15 == 0 && i > 0) {
                    largeList.clear()
                    System.gc() // Принудительный вызов GC для анализа
                    logMessage("Forced GC called, cleared ${largeList.size} objects")
                    totalAllocated = 0
                }
            }

            Trace.endSection()
            logMessage("Memory тест завершен!")
        }
    }

    private fun performAsyncWork() {
        profiler.measureTime("performAsyncWork") {
            Trace.beginSection("Async_Work")

            lifecycleScope.launch {
                repeat(3) { i ->
                    withContext(Dispatchers.IO) {
                        profiler.measureTime("IO_Operation_$i") {
                            Trace.beginSection("IO_Operation_$i")
                            logMessage("Выполняем IO операцию $i...")
                            // Имитация IO операции
                            delay(100)
                            performDiskSimulation()
                            Trace.endSection()
                        }
                    }

                    withContext(Dispatchers.Default) {
                        profiler.measureTime("Computation_$i") {
                            Trace.beginSection("Computation_$i")
                            logMessage("Выполняем вычисления $i...")
                            // Вычислительная работа в фоне
                            calculatePrimes(500)
                            Trace.endSection()
                        }
                    }
                }
                logMessage("Async тест завершен!")
            }

            Trace.endSection()
        }
    }

    private fun calculateFibonacci(n: Int): Long {
        return if (n <= 1) n.toLong()
        else calculateFibonacci(n - 1) + calculateFibonacci(n - 2)
    }

    private fun performDiskSimulation() {
        // Имитация дисковых операций
        val data = ByteArray(8192) { Random.nextInt().toByte() }
        val tempFile = createTempFile("profiler_test", ".tmp")
        tempFile.writeBytes(data)
        val readData = tempFile.readBytes()
        tempFile.delete()
    }

    private fun calculatePrimes(limit: Int): List<Int> {
        val primes = mutableListOf<Int>()
        val isPrime = BooleanArray(limit + 1) { true }

        for (i in 2..limit) {
            if (isPrime[i]) {
                primes.add(i)
                for (j in i * i..limit step i) {
                    isPrime[j] = false
                }
            }
        }
        return primes
    }

    override fun onDestroy() {
        super.onDestroy()
        // Останавливаем трейсинг
        Debug.stopMethodTracing()
    }
}