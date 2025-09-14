package com.example.multiplatform.benchmark

// JavaScript реализация - оптимизации для V8
actual class PlatformOptimizer actual constructor() {

    actual fun optimizedSort(list: List<Int>): List<Int> {
        // V8 оптимизирует встроенную сортировку лучше чем пользовательский код
        return list.toIntArray().apply { sort() }.toList()
    }

    actual fun optimizedStringConcat(items: List<String>): String {
        // Для JS эффективнее Array.join чем множественные конкатенации
        return when {
            items.size < 10 -> items.joinToString(", ")
            else -> {
                val builder = StringBuilder()
                for (i in items.indices) {
                    if (i > 0) builder.append(", ")
                    builder.append(items[i])
                }
                builder.toString()
            }
        }
    }

    actual fun heavyComputation(n: Int): Long {
        // Избегаем создания промежуточных коллекций в JS
        var sum = 0L
        var i = 1L
        while (i <= n) {
            sum += i * i
            i++
        }
        return sum
    }
}