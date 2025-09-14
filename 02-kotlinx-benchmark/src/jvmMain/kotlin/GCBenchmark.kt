package com.example.benchmark

import kotlinx.benchmark.*

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MILLISECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
class GCBenchmark {

    @Param("1000", "10000", "100000")
    var size: Int = 0

    @Setup
    fun setup() {
        // Принудительный GC перед тестом
        System.gc()
    }

    @Benchmark
    fun allocateStrings(): String {
        return (0 until size).joinToString { "item$it" }
    }

    @Benchmark
    fun allocateStringBuilder(): String {
        val builder = StringBuilder()
        repeat(size) {
            builder.append("item$it")
            if (it < size - 1) builder.append(", ")
        }
        return builder.toString()
    }

    @Benchmark
    fun allocateList(): List<String> {
        return (0 until size).map { "item$it" }
    }
}