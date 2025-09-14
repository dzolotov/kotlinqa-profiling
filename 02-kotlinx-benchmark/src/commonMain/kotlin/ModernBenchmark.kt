package com.example.benchmark

import kotlinx.benchmark.*

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
class ModernBenchmark {

    @Param("100", "1000", "10000")
    var size: Int = 0

    private lateinit var data: List<Int>

    @Setup
    fun setup() {
        data = List(size) { it }
    }

    @Benchmark
    fun sumSequential(): Long = data.sum().toLong()

    @Benchmark
    fun sumParallel(): Long = data.asSequence().sum().toLong()

    @Benchmark
    fun filterMap(): List<String> = data.filter { it % 2 == 0 }.map { "item$it" }

    @Benchmark
    fun sequence(): List<String> = data.asSequence()
        .filter { it % 2 == 0 }
        .map { "item$it" }
        .toList()
}