package com.example.multiplatform.benchmark

// Expect/Actual для платформенных оптимизаций
expect class PlatformOptimizer() {
    fun optimizedSort(list: List<Int>): List<Int>
    fun optimizedStringConcat(items: List<String>): String
    fun heavyComputation(n: Int): Long
}