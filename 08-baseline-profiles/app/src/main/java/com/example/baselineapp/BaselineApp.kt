package com.example.baselineapp

import android.app.Application
import android.util.Log

/**
 * Application класс - важный для Baseline Profile
 * Здесь происходит инициализация компонентов приложения
 */
class BaselineApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("BaselineApp", "Application onCreate - начало инициализации")

        // Симуляция инициализации компонентов
        initializeLogger()
        initializeNetworking()
        initializeImageLoader()
        initializeDependencies()

        Log.d("BaselineApp", "Application onCreate - инициализация завершена")
    }

    /**
     * Инициализация системы логирования
     * Этот метод попадет в Baseline Profile как "горячий"
     */
    private fun initializeLogger() {
        // Симуляция настройки логирования
        repeat(100) {
            // Некоторая работа для симуляции реальной инициализации
            val dummy = "Logger setup iteration $it"
        }
        Log.d("BaselineApp", "Logger инициализирован")
    }

    /**
     * Инициализация сетевых компонентов
     * Критически важно для производительности
     */
    private fun initializeNetworking() {
        repeat(150) {
            val dummy = "Network setup iteration $it"
        }
        Log.d("BaselineApp", "Network компоненты инициализированы")
    }

    /**
     * Инициализация загрузчика изображений
     */
    private fun initializeImageLoader() {
        repeat(80) {
            val dummy = "ImageLoader setup iteration $it"
        }
        Log.d("BaselineApp", "ImageLoader инициализирован")
    }

    /**
     * Инициализация зависимостей
     */
    private fun initializeDependencies() {
        repeat(120) {
            val dummy = "Dependencies setup iteration $it"
        }
        Log.d("BaselineApp", "Зависимости инициализированы")
    }
}