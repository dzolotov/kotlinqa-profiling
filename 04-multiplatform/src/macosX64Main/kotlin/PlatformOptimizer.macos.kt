package com.example.multiplatform.benchmark

// Native macOS реализация с оптимизациями
actual class PlatformOptimizer actual constructor() {

    actual fun optimizedSort(list: List<Int>): List<Int> {
        return if (list.size < 50) {
            insertionSort(list)
        } else {
            list.sorted()
        }
    }

    actual fun optimizedStringConcat(items: List<String>): String {
        if (items.isEmpty()) return ""
        if (items.size == 1) return items[0]

        val totalLength = items.sumOf { it.length } + (items.size - 1) * 2
        val result = StringBuilder(totalLength)

        items.forEachIndexed { index, item ->
            if (index > 0) result.append(", ")
            result.append(item)
        }

        return result.toString()
    }

    actual fun heavyComputation(n: Int): Long {
        // Развертка цикла для лучшей производительности
        var sum = 0L
        val limit = n.toLong()
        var i = 1L

        while (i <= limit - 3) {
            sum += i * i + (i + 1) * (i + 1) + (i + 2) * (i + 2) + (i + 3) * (i + 3)
            i += 4
        }

        while (i <= limit) {
            sum += i * i
            i++
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