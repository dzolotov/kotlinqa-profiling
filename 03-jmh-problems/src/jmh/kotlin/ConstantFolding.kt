package com.example.jmh.problems

import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit
import kotlin.math.PI
import kotlin.random.Random

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
open class ConstantFolding {

    private val random = Random.Default
    private val x = 2
    private val y = 2

    @Benchmark
    fun wrongConstantFolding(): Int {
        // ❌ НЕПРАВИЛЬНО - компилятор заменит на константу 4
        return 2 * 2
    }

    @Benchmark
    fun correctWithFields(): Int {
        // ✅ ПРАВИЛЬНО - использует поля класса
        return x * y
    }

    @Benchmark
    fun correctWithRandom(): Int {
        // ✅ ПРАВИЛЬНО - использует случайные значения
        val a = random.nextInt(10)
        val b = random.nextInt(10)
        return a * b
    }

    @Param("2", "3", "5")
    var parameter: Int = 0

    @Benchmark
    fun correctWithParam(): Int {
        // ✅ ПРАВИЛЬНО - использует параметры бенчмарка
        return parameter * parameter
    }

    @Benchmark
    fun wrongMathConstant(): Double {
        // ❌ НЕПРАВИЛЬНО - PI это константа
        return PI * 2
    }

    @Benchmark
    fun correctMathCalculation(): Double {
        // ✅ ПРАВИЛЬНО - вычисление с переменными
        val radius = x.toDouble()
        return PI * radius * radius
    }

    @Benchmark
    fun wrongStringConstant(): String {
        // ❌ НЕПРАВИЛЬНО - строковая константа будет заменена компилятором
        return "Hello" + " " + "World"
    }

    @Benchmark
    fun correctStringCalculation(): String {
        // ✅ ПРАВИЛЬНО - динамическое построение строки
        val greeting = "Hello"
        val target = if (random.nextBoolean()) "World" else "Kotlin"
        return "$greeting $target"
    }

    @Benchmark
    fun wrongListConstant(): List<Int> {
        // ❌ НЕПРАВИЛЬНО - список констант может быть оптимизирован
        return listOf(1, 2, 3, 4, 5)
    }

    @Benchmark
    fun correctListGeneration(): List<Int> {
        // ✅ ПРАВИЛЬНО - динамическое создание списка
        return (1..parameter).toList()
    }
}