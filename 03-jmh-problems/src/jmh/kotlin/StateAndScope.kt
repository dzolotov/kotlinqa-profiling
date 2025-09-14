package com.example.jmh.problems

import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

// ❌ НЕПРАВИЛЬНО - общее изменяемое состояние между потоками
@State(Scope.Benchmark) // Разделяется между потоками
open class WrongState {
    private var counter = 0

    fun increment(): Int {
        return ++counter // Race condition!
    }
}

// ✅ ПРАВИЛЬНО - thread-local состояние
@State(Scope.Thread)
open class CorrectState {
    private var counter = 0

    fun increment(): Int {
        return ++counter // Безопасно
    }
}

// ✅ ПРАВИЛЬНО - состояние уровня группы с thread-safe операциями
@State(Scope.Group)
open class GroupState {
    @Volatile
    private var sharedValue = 0

    // Для демонстрации более сложного сценария
    private val atomicCounter = AtomicInteger(0)

    fun setValue(value: Int) {
        sharedValue = value
    }

    fun getValue(): Int = sharedValue

    fun incrementAtomic(): Int = atomicCounter.incrementAndGet()

    fun getAtomicValue(): Int = atomicCounter.get()
}

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
open class StateAndScope {

    @Benchmark
    fun wrongBenchmark(state: WrongState): Int {
        // ❌ Race condition при многопоточности
        return state.increment()
    }

    @Benchmark
    fun correctBenchmark(state: CorrectState): Int {
        // ✅ Каждый поток имеет свою копию состояния
        return state.increment()
    }

    @Group("readWrite")
    @Benchmark
    fun writer(state: GroupState) {
        // Записываем значение
        state.setValue(42)
        state.incrementAtomic()
    }

    @Group("readWrite")
    @Benchmark
    fun reader(state: GroupState): Int {
        // Читаем значения
        val simpleValue = state.getValue()
        val atomicValue = state.getAtomicValue()
        return simpleValue + atomicValue
    }

    // Демонстрация проблем с Benchmark scope при многопоточности
    @Benchmark
    @Threads(4) // Запускаем в 4 потока
    fun wrongMultiThreaded(state: WrongState): Int {
        // ❌ ОПАСНО - 4 потока модифицируют общий счетчик
        return state.increment()
    }

    @Benchmark
    @Threads(4) // Запускаем в 4 потока
    fun correctMultiThreaded(state: CorrectState): Int {
        // ✅ БЕЗОПАСНО - каждый поток имеет свой счетчик
        return state.increment()
    }
}