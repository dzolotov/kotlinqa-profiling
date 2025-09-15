package com.example.profiler

import android.os.Bundle
import android.os.Trace
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import kotlin.random.Random
import kotlin.system.measureNanoTime

/**
 * Демонстрационное Activity для Android Profiler
 * Показывает различные аспекты профилирования:
 * - CPU профилирование
 * - Memory профилирование
 * - Network профилирование
 * - Energy профилирование
 */
class ProfiledActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Симуляция утечки памяти - статическая ссылка на Activity
    companion object {
        private var leakedActivities = mutableListOf<ProfiledActivity>()
        private const val TAG = "ProfiledActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Начинаем трейсинг onCreate
        Trace.beginSection("ProfiledActivity.onCreate")
        try {
            setContentView(R.layout.activity_profiled)

            statusText = findViewById(R.id.statusText)

            // Симулируем потенциальную утечку памяти
            leakedActivities.add(this) // ❌ Утечка памяти!

            setupButtons()

            // Инициализация с тяжелой операцией
            performHeavyInitialization()

        } finally {
            Trace.endSection()
        }
    }

    /**
     * Настройка кнопок для различных типов профилирования
     */
    private fun setupButtons() {
        findViewById<Button>(R.id.btnCpuIntensive).setOnClickListener {
            performCpuIntensiveTask()
        }

        findViewById<Button>(R.id.btnMemoryIntensive).setOnClickListener {
            performMemoryIntensiveTask()
        }

        findViewById<Button>(R.id.btnNetworkIntensive).setOnClickListener {
            performNetworkIntensiveTask()
        }

        findViewById<Button>(R.id.btnEnergyIntensive).setOnClickListener {
            performEnergyIntensiveTask()
        }

        findViewById<Button>(R.id.btnTriggerGC).setOnClickListener {
            triggerGarbageCollection()
        }
    }

    /**
     * CPU-интенсивная операция
     * Видно в CPU Profiler как горячую точку
     */
    private fun performCpuIntensiveTask() {
        Trace.beginAsyncSection("CPU Intensive Task", 1001)

        scope.launch(Dispatchers.Default) {
            val startTime = System.nanoTime()

            // Сортировка большого массива множество раз
            val result = withContext(Dispatchers.Default) {
                repeat(100) { iteration ->
                    val array = IntArray(10000) { Random.nextInt() }

                    // Method tracing для детального анализа
                    Trace.beginSection("BubbleSort_$iteration")
                    bubbleSort(array) // Неэффективная сортировка O(n²)
                    Trace.endSection()

                    // Fibonacci для CPU нагрузки
                    Trace.beginSection("Fibonacci_$iteration")
                    calculateFibonacci(35) // Рекурсивный Fibonacci - очень медленно!
                    Trace.endSection()
                }

                "CPU Task completed"
            }

            val duration = (System.nanoTime() - startTime) / 1_000_000

            withContext(Dispatchers.Main) {
                statusText.text = "$result in ${duration}ms"
                Log.d(TAG, "CPU intensive task completed in ${duration}ms")
            }
        }

        Trace.endAsyncSection("CPU Intensive Task", 1001)
    }

    /**
     * Memory-интенсивная операция
     * Создает много объектов для анализа в Memory Profiler
     */
    private fun performMemoryIntensiveTask() {
        Trace.beginSection("Memory Intensive Task")

        scope.launch {
            val largeObjects = mutableListOf<ByteArray>()

            try {
                // Аллокация больших объектов
                repeat(50) { i ->
                    val size = 1024 * 1024 // 1MB
                    val largeArray = ByteArray(size) { (it % 256).toByte() }
                    largeObjects.add(largeArray)

                    // Создаем временные объекты для GC pressure
                    repeat(1000) {
                        val temp = String(ByteArray(1024) { Random.nextInt(256).toByte() })
                        temp.hashCode() // Используем, чтобы не оптимизировалось
                    }

                    statusText.text = "Allocated ${i + 1} MB"
                    delay(100) // Даем время для отображения в Memory Profiler
                }

                // Симуляция memory churn
                performMemoryChurn()

            } finally {
                // Очищаем память (но утечка через companion object остается!)
                largeObjects.clear()
                statusText.text = "Memory task completed. Check Memory Profiler!"
            }
        }

        Trace.endSection()
    }

    /**
     * Симуляция Memory Churn - частое создание/удаление объектов
     */
    private suspend fun performMemoryChurn() {
        Trace.beginSection("Memory Churn")

        repeat(100) {
            // Создаем много временных объектов
            val tempList = List(1000) { index ->
                DataClass(
                    id = index,
                    name = "Object_$index",
                    data = ByteArray(1024)
                )
            }

            // Используем объекты
            tempList.forEach { it.hashCode() }

            delay(10) // Небольшая задержка для визуализации
        }

        Trace.endSection()
    }

    /**
     * Network-интенсивная операция
     * Симулирует сетевую активность для Network Profiler
     */
    private fun performNetworkIntensiveTask() {
        scope.launch {
            Trace.beginAsyncSection("Network Task", 2001)

            try {
                // Симуляция множественных сетевых запросов
                val results = withContext(Dispatchers.IO) {
                    val responses = mutableListOf<String>()

                    repeat(10) { i ->
                        // Симуляция API вызова
                        delay(Random.nextLong(100, 500))
                        responses.add("Response_$i")

                        withContext(Dispatchers.Main) {
                            statusText.text = "Network request ${i + 1}/10"
                        }
                    }

                    responses
                }

                statusText.text = "Network: ${results.size} responses received"

            } finally {
                Trace.endAsyncSection("Network Task", 2001)
            }
        }
    }

    /**
     * Energy-интенсивная операция
     * Использует GPS, камеру, яркость экрана
     */
    private fun performEnergyIntensiveTask() {
        scope.launch {
            Trace.beginSection("Energy Intensive Task")

            // Симуляция использования GPS
            repeat(10) {
                Log.d(TAG, "Simulating GPS usage: location $it")
                delay(100)
            }

            // Симуляция использования камеры
            Log.d(TAG, "Simulating camera usage")

            // CPU + GPU нагрузка
            performCpuIntensiveTask()

            statusText.text = "Energy intensive task running. Check Energy Profiler!"

            Trace.endSection()
        }
    }

    /**
     * Принудительная сборка мусора для анализа в Memory Profiler
     */
    private fun triggerGarbageCollection() {
        statusText.text = "Triggering GC..."

        // Запрашиваем сборку мусора
        System.gc()
        System.runFinalization()
        System.gc()

        // Логируем информацию о памяти
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
        val maxMemory = runtime.maxMemory() / 1024 / 1024

        val memInfo = "Memory: $usedMemory MB / $maxMemory MB"
        statusText.text = memInfo
        Log.d(TAG, memInfo)
    }

    /**
     * Тяжелая инициализация при старте
     * Влияет на время запуска приложения
     */
    private fun performHeavyInitialization() {
        Trace.beginSection("Heavy Initialization")

        // Симуляция загрузки больших ресурсов
        Thread.sleep(100) // ❌ Блокирует UI поток!

        // Инициализация "тяжелых" компонентов
        repeat(1000) {
            val temp = HashMap<String, String>()
            repeat(100) { j ->
                temp["key_$j"] = "value_$j"
            }
        }

        Trace.endSection()
    }

    /**
     * Неэффективная сортировка пузырьком для CPU профилирования
     */
    private fun bubbleSort(arr: IntArray) {
        val n = arr.size
        for (i in 0 until n - 1) {
            for (j in 0 until n - i - 1) {
                if (arr[j] > arr[j + 1]) {
                    val temp = arr[j]
                    arr[j] = arr[j + 1]
                    arr[j + 1] = temp
                }
            }
        }
    }

    /**
     * Рекурсивный Fibonacci - неэффективно для больших чисел
     */
    private fun calculateFibonacci(n: Int): Long {
        return if (n <= 1) n.toLong()
        else calculateFibonacci(n - 1) + calculateFibonacci(n - 2)
    }

    /**
     * Вспомогательный data class для memory профилирования
     */
    data class DataClass(
        val id: Int,
        val name: String,
        val data: ByteArray
    )

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()

        // ❌ Забыли удалить из статического списка - утечка памяти!
        // leakedActivities.remove(this) // Нужно раскомментировать!
    }
}