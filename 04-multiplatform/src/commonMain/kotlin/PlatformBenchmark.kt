package com.example.multiplatform.benchmark

import kotlinx.benchmark.*

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MICROSECONDS)
class PlatformBenchmark {

    @Param("1000", "10000", "100000")
    var size: Int = 0

    private lateinit var data: List<Int>
    private lateinit var strings: List<String>
    private val optimizer = PlatformOptimizer()

    @Setup
    fun prepare() {
        data = (0 until size).shuffled()
        strings = (0 until size).map { "string$it" }
    }

    @Benchmark
    fun platformSort(): List<Int> = optimizer.optimizedSort(data)

    @Benchmark
    fun standardSort(): List<Int> = data.sorted()

    @Benchmark
    fun platformStringConcat(): String = optimizer.optimizedStringConcat(strings)

    @Benchmark
    fun standardStringConcat(): String = strings.joinToString(", ")

    @Benchmark
    fun platformHeavyComputation(): Long = optimizer.heavyComputation(size / 100)

    @Benchmark
    fun standardHeavyComputation(): Long {
        val n = size / 100
        return (1..n.toLong()).fold(0L) { acc, i -> acc + i * i }
    }
}