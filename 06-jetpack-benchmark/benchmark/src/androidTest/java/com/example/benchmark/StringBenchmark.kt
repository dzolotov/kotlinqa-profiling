package com.example.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StringBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun stringConcatenation() = benchmarkRule.measureRepeated {
        var result = ""
        repeat(100) {
            result += "item$it"
        }
        // Prevent optimization
        if (result.isEmpty()) {
            throw RuntimeException("Should not be empty")
        }
    }

    @Test
    fun stringBuilder() = benchmarkRule.measureRepeated {
        val builder = StringBuilder()
        repeat(100) {
            builder.append("item$it")
        }
        val result = builder.toString()
        // Prevent optimization
        if (result.isEmpty()) {
            throw RuntimeException("Should not be empty")
        }
    }

    @Test
    fun stringJoin() = benchmarkRule.measureRepeated {
        val items = (0 until 100).map { "item$it" }
        val result = items.joinToString("")
        // Prevent optimization
        if (result.isEmpty()) {
            throw RuntimeException("Should not be empty")
        }
    }
}