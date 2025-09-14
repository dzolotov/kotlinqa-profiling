package com.example.profiler

import android.os.SystemClock
import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class CustomProfiler private constructor() {

    companion object {
        private const val TAG = "CustomProfiler"

        @Volatile
        private var INSTANCE: CustomProfiler? = null

        fun getInstance(): CustomProfiler {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CustomProfiler().also { INSTANCE = it }
            }
        }
    }

    private val methodTimings = ConcurrentHashMap<String, AtomicLong>()
    private val methodCounts = ConcurrentHashMap<String, AtomicLong>()
    private val activeTimers = ConcurrentHashMap<String, Long>()

    fun startTiming(methodName: String) {
        val startTime = SystemClock.elapsedRealtime()
        activeTimers[methodName] = startTime

        Log.d(TAG, "Начало выполнения: $methodName в ${System.currentTimeMillis()}")
    }

    fun endTiming(methodName: String) {
        val endTime = SystemClock.elapsedRealtime()
        val startTime = activeTimers.remove(methodName)

        if (startTime != null) {
            val duration = endTime - startTime

            // Обновляем статистику
            methodTimings.computeIfAbsent(methodName) { AtomicLong(0) }.addAndGet(duration)
            methodCounts.computeIfAbsent(methodName) { AtomicLong(0) }.incrementAndGet()

            Log.d(TAG, "Завершение выполнения: $methodName за ${duration}ms")
        }
    }

    fun getMethodStatistics(): Map<String, MethodStats> {
        val stats = mutableMapOf<String, MethodStats>()

        for (methodName in methodTimings.keys) {
            val totalTime = methodTimings[methodName]?.get() ?: 0
            val callCount = methodCounts[methodName]?.get() ?: 0
            val averageTime = if (callCount > 0) totalTime / callCount else 0

            stats[methodName] = MethodStats(
                methodName = methodName,
                totalTimeMs = totalTime,
                callCount = callCount,
                averageTimeMs = averageTime
            )
        }

        return stats
    }

    fun logStatistics() {
        Log.i(TAG, "=== Статистика профилирования ===")
        val stats = getMethodStatistics().values.sortedByDescending { it.totalTimeMs }

        for (stat in stats) {
            Log.i(TAG, "${stat.methodName}: " +
                    "всего ${stat.totalTimeMs}ms, " +
                    "вызовов ${stat.callCount}, " +
                    "среднее ${stat.averageTimeMs}ms")
        }
    }

    fun reset() {
        methodTimings.clear()
        methodCounts.clear()
        activeTimers.clear()
        Log.d(TAG, "Статистика профилирования очищена")
    }

    data class MethodStats(
        val methodName: String,
        val totalTimeMs: Long,
        val callCount: Long,
        val averageTimeMs: Long
    )
}

// Annotation для автоматического профилирования
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ProfileMethod(val name: String = "")

// Extension функции для удобного использования
inline fun <T> CustomProfiler.measureTime(methodName: String, block: () -> T): T {
    startTiming(methodName)
    return try {
        block()
    } finally {
        endTiming(methodName)
    }
}

// Пример использования
class ExampleUsage {

    private val profiler = CustomProfiler.getInstance()

    @ProfileMethod("heavyComputation")
    fun performHeavyComputation() {
        profiler.measureTime("performHeavyComputation") {
            // Тяжелые вычисления
            Thread.sleep(1000)
            calculateSomething()
        }
    }

    private fun calculateSomething() {
        profiler.measureTime("calculateSomething") {
            repeat(10000) {
                Math.sqrt(it.toDouble())
            }
        }
    }

    fun showStatistics() {
        profiler.logStatistics()
    }
}