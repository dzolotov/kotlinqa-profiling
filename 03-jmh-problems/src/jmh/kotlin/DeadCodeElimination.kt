package com.example.jmh.problems

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import kotlin.math.ln

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
open class DeadCodeElimination {

    @Benchmark
    fun wrong() {
        // ❌ НЕПРАВИЛЬНО - результат не используется, JVM может убрать вызов!
        ln(42.0)
    }

    @Benchmark
    fun correct(): Double {
        // ✅ ПРАВИЛЬНО - результат потребляется
        return ln(42.0)
    }

    @Benchmark
    fun correctBlackhole(bh: Blackhole) {
        // ✅ ПРАВИЛЬНО - используем Blackhole для потребления результата
        bh.consume(ln(42.0))
    }

    @Benchmark
    fun wrongLoop() {
        // ❌ НЕПРАВИЛЬНО - пустой цикл может быть оптимизирован
        for (i in 0 until 1000) {
            // пустое тело цикла
        }
    }

    @Benchmark
    fun correctLoop(bh: Blackhole) {
        // ✅ ПРАВИЛЬНО - результат каждой итерации потребляется
        for (i in 0 until 1000) {
            bh.consume(i)
        }
    }

    @Benchmark
    fun wrongStringBuilder() {
        // ❌ НЕПРАВИЛЬНО - создаем StringBuilder, но результат не используем
        val builder = StringBuilder()
        repeat(100) { i ->
            builder.append("item$i")
        }
        // Не возвращаем результат - может быть оптимизировано!
    }

    @Benchmark
    fun correctStringBuilder(): String {
        // ✅ ПРАВИЛЬНО - возвращаем результат
        val builder = StringBuilder()
        repeat(100) { i ->
            builder.append("item$i")
        }
        return builder.toString()
    }

    @Benchmark
    fun correctStringBuilderBlackhole(bh: Blackhole) {
        // ✅ ПРАВИЛЬНО - потребляем результат через Blackhole
        val builder = StringBuilder()
        repeat(100) { i ->
            builder.append("item$i")
        }
        bh.consume(builder.toString())
    }
}