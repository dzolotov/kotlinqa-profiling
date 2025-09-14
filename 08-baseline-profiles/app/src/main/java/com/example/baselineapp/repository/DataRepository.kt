package com.example.baselineapp.repository

import android.util.Log
import com.example.baselineapp.model.DataItem
import kotlinx.coroutines.delay

/**
 * Репозиторий данных - часто используемый компонент для Baseline Profile
 */
object DataRepository {

    /**
     * Загружает список элементов данных
     * Этот метод попадет в Baseline Profile как часто используемый
     */
    suspend fun loadItemList(): List<DataItem> {
        Log.d("DataRepository", "Loading item list...")

        // Симуляция сетевого запроса или загрузки из базы данных
        delay(200)

        // Генерируем тестовые данные
        val items = (1..50).map { index ->
            DataItem(
                id = index.toString(),
                title = "Item $index",
                description = "This is a detailed description for item number $index. " +
                        "It contains multiple lines of text to demonstrate scrolling " +
                        "performance and memory allocation patterns in the Baseline Profile."
            )
        }

        // Дополнительная обработка данных
        performDataProcessing(items)

        Log.d("DataRepository", "Item list loaded: ${items.size} items")
        return items
    }

    /**
     * Загружает детали конкретного элемента
     */
    suspend fun loadItemDetails(itemId: String): DataItem {
        Log.d("DataRepository", "Loading details for item: $itemId")

        // Симуляция более долгого запроса для деталей
        delay(150)

        val item = DataItem(
            id = itemId,
            title = "Detailed Item $itemId",
            description = "This is a comprehensive description for item $itemId. " +
                    "It includes detailed information that would be shown on a detail screen. " +
                    "The text is longer to simulate real-world scenarios where detail screens " +
                    "contain substantial content that needs to be processed and rendered efficiently."
        )

        // Дополнительная обработка для деталей
        performDetailProcessing(item)

        Log.d("DataRepository", "Item details loaded for: $itemId")
        return item
    }

    /**
     * Обработка данных списка
     * Попадет в Baseline Profile из-за частого использования
     */
    private fun performDataProcessing(items: List<DataItem>) {
        Log.d("DataRepository", "Processing ${items.size} items...")

        // Симуляция обработки данных (фильтрация, сортировка, группировка)
        items.forEach { item ->
            val processedTitle = item.title.uppercase()
            val processedDescription = item.description.take(100) + "..."

            // Некоторые вычисления для имитации реальной обработки
            val hashValue = (item.id.hashCode() + processedTitle.hashCode()) % 1000
        }

        Log.d("DataRepository", "Data processing completed")
    }

    /**
     * Обработка деталей элемента
     */
    private fun performDetailProcessing(item: DataItem) {
        Log.d("DataRepository", "Processing details for item: ${item.id}")

        // Симуляция более сложной обработки для экрана деталей
        repeat(50) { iteration ->
            val calculation = item.id.hashCode() + iteration * 2
            val formatted = "Detail processing iteration $iteration: $calculation"
        }

        Log.d("DataRepository", "Detail processing completed")
    }

    /**
     * Поиск элементов по запросу
     * Метод для демонстрации поисковых сценариев в Baseline Profile
     */
    suspend fun searchItems(query: String): List<DataItem> {
        Log.d("DataRepository", "Searching for: $query")

        // Симуляция поиска
        delay(100)

        val allItems = loadItemList()
        val filteredItems = allItems.filter { item ->
            item.title.contains(query, ignoreCase = true) ||
            item.description.contains(query, ignoreCase = true)
        }

        Log.d("DataRepository", "Search completed: ${filteredItems.size} results")
        return filteredItems
    }
}