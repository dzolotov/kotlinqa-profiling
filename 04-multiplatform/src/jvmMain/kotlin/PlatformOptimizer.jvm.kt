package com.example.multiplatform.benchmark

import java.util.stream.Collectors
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveTask
import kotlin.math.min

// JVM реализация с многопоточностью и оптимизациями
actual class PlatformOptimizer actual constructor() {

    actual fun optimizedSort(list: List<Int>): List<Int> {
        // Используем параллельную сортировку для больших коллекций
        return if (list.size > 1000) {
            list.parallelStream()
                .sorted()
                .collect(Collectors.toList())
        } else {
            list.sorted()
        }
    }

    actual fun optimizedStringConcat(items: List<String>): String {
        // Используем StringBuilder для лучшей производительности
        return if (items.size > 100) {
            items.parallelStream()
                .collect(Collectors.joining(", "))
        } else {
            items.joinToString(", ")
        }
    }

    actual fun heavyComputation(n: Int): Long {
        return if (n > 1000) {
            // Используем Fork/Join для параллельных вычислений
            ForkJoinPool.commonPool().invoke(SumSquaresTask(1, n.toLong()))
        } else {
            (1..n.toLong()).fold(0L) { acc, i -> acc + i * i }
        }
    }

    private class SumSquaresTask(private val start: Long, private val end: Long) : RecursiveTask<Long>() {
        override fun compute(): Long {
            val length = end - start
            return if (length <= 1000) {
                // Базовый случай: вычисляем напрямую
                (start..end).fold(0L) { acc, i -> acc + i * i }
            } else {
                // Разделяем задачу
                val mid = start + length / 2
                val leftTask = SumSquaresTask(start, mid)
                val rightTask = SumSquaresTask(mid + 1, end)

                leftTask.fork()
                val rightResult = rightTask.compute()
                val leftResult = leftTask.join()

                leftResult + rightResult
            }
        }
    }
}