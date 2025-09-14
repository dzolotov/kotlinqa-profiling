package com.example.jmh.problems

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
open class KotlinSpecificProblems {

    private val testData = (1..1000).toList()

    @Benchmark
    fun wrongLambdaAllocation(): List<String> {
        // ❌ НЕПРАВИЛЬНО - создается новая лямбда каждый раз
        return testData.map { "item$it" }
    }

    private val mapFunction: (Int) -> String = { "item$it" }

    @Benchmark
    fun correctPrecomputedLambda(): List<String> {
        // ✅ ПРАВИЛЬНО - переиспользуем лямбду
        return testData.map(mapFunction)
    }

    @Benchmark
    fun wrongStringTemplate(): List<String> {
        // ❌ МОЖЕТ БЫТЬ ПРОБЛЕМОЙ - string templates создают StringBuilder
        return testData.map { "prefix_${it}_suffix" }
    }

    @Benchmark
    fun correctStringConcatenation(): List<String> {
        // ✅ АЛЬТЕРНАТИВА - прямая конкатенация для простых случаев
        return testData.map { "prefix_$it" + "_suffix" }
    }

    @Benchmark
    fun wrongDataClassCopy(): List<TestData> {
        // ❌ НЕПРАВИЛЬНО - copy() создает новые объекты без необходимости
        val original = TestData("test", 42)
        return (1..100).map { original.copy(id = it) }
    }

    @Benchmark
    fun correctDirectConstruction(): List<TestData> {
        // ✅ ПРАВИЛЬНО - прямое создание объектов
        return (1..100).map { TestData("test", it) }
    }

    @Benchmark
    fun wrongWhenExpression(): Int {
        // ❌ НЕПРАВИЛЬНО - when с константами может быть оптимизирован
        val value = 5
        return when (value) {
            1 -> 10
            2 -> 20
            5 -> 50
            else -> 0
        }
    }

    @Param("1", "2", "5", "10")
    var dynamicValue: Int = 0

    @Benchmark
    fun correctWhenExpression(): Int {
        // ✅ ПРАВИЛЬНО - when с динамическими значениями
        return when (dynamicValue) {
            1 -> 10
            2 -> 20
            5 -> 50
            else -> 0
        }
    }

    @Benchmark
    fun wrongInlineFunction() {
        // ❌ НЕПРАВИЛЬНО - результат inline функции не используется
        repeat(100) {
            // пустое тело может быть оптимизировано
        }
    }

    @Benchmark
    fun correctInlineFunction(bh: Blackhole) {
        // ✅ ПРАВИЛЬНО - потребляем результат каждой итерации
        repeat(100) { i ->
            bh.consume(i * i)
        }
    }

    @Benchmark
    fun wrongExtensionFunction(): List<Int> {
        // ❌ МОЖЕТ БЫТЬ ПРОБЛЕМОЙ - создание промежуточных коллекций
        return testData
            .filter { it % 2 == 0 }
            .map { it * 2 }
            .take(10)
    }

    @Benchmark
    fun correctSequenceChain(): List<Int> {
        // ✅ ПРАВИЛЬНО - ленивые вычисления через sequences
        return testData.asSequence()
            .filter { it % 2 == 0 }
            .map { it * 2 }
            .take(10)
            .toList()
    }

    @Benchmark
    fun wrongNullableOperations(): List<String> {
        // ❌ НЕПРАВИЛЬНО - лишние проверки на null для non-null значений
        return testData.map { value ->
            value?.let { "item$it" } ?: "empty"
        }
    }

    @Benchmark
    fun correctNonNullOperations(): List<String> {
        // ✅ ПРАВИЛЬНО - прямая работа с non-null значениями
        return testData.map { "item$it" }
    }
}

data class TestData(val name: String, val id: Int)