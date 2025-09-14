package com.example.multiplatform.benchmark

// Native Linux реализация с оптимизациями для x64
actual class PlatformOptimizer actual constructor() {

    actual fun optimizedSort(list: List<Int>): List<Int> {
        // Native код использует эффективные алгоритмы сортировки LLVM
        return when {
            list.size < 50 -> insertionSort(list)
            else -> list.sorted()
        }
    }

    actual fun optimizedStringConcat(items: List<String>): String {
        // Native реализация с минимальными аллокациями
        if (items.isEmpty()) return ""
        if (items.size == 1) return items[0]

        val totalLength = items.sumOf { it.length } + (items.size - 1) * 2 // для ", "
        val result = StringBuilder(totalLength)

        items.forEachIndexed { index, item ->
            if (index > 0) result.append(", ")
            result.append(item)
        }

        return result.toString()
    }

    actual fun heavyComputation(n: Int): Long {
        // Оптимизированное вычисление с минимизацией overhead
        var sum = 0L
        for (i in 1..n.toLong()) {
            sum += i * i
        }
        return sum
    }

    private fun insertionSort(list: List<Int>): List<Int> {
        val array = list.toIntArray()
        for (i in 1 until array.size) {
            val key = array[i]
            var j = i - 1
            while (j >= 0 && array[j] > key) {
                array[j + 1] = array[j]
                j--
            }
            array[j + 1] = key
        }
        return array.toList()
    }
}