package com.example.multiplatform.benchmark

import kotlinx.benchmark.*

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MICROSECONDS)
class CollectionBenchmark {

    @Param("1000", "10000", "100000")
    var size: Int = 0

    private lateinit var data: List<Int>

    @Setup
    fun prepare() {
        data = (0 until size).toList()
    }

    @Benchmark
    fun filterMap(): List<String> =
        data.filter { it % 2 == 0 }.map { "item$it" }

    @Benchmark
    fun sequence(): List<String> = data.asSequence()
        .filter { it % 2 == 0 }
        .map { "item$it" }
        .toList()

    @Benchmark
    fun streamingOperations(): Int =
        data.filter { it > 500 }
            .map { it * 2 }
            .filter { it < 10000 }
            .sum()

    @Benchmark
    fun sequenceOperations(): Int = data.asSequence()
        .filter { it > 500 }
        .map { it * 2 }
        .filter { it < 10000 }
        .sum()
}