package com.example.baselineapp.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.baselineapp.ui.screens.ListScreen
import com.example.baselineapp.ui.screens.DetailScreen
import com.example.baselineapp.ui.theme.BaselineAppTheme

/**
 * Главная активность - критический компонент для Baseline Profile
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate started")

        // Выполняем инициализацию UI
        performUIInitialization()

        setContent {
            BaselineAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BaselineAppNavigation()
                }
            }
        }

        Log.d("MainActivity", "onCreate completed")
    }

    /**
     * Инициализация UI компонентов
     * Этот метод попадет в Baseline Profile
     */
    private fun performUIInitialization() {
        Log.d("MainActivity", "UI initialization started")

        // Симуляция тяжелой инициализации UI
        repeat(200) {
            val dummy = "UI setup iteration $it"
            // Некоторые вычисления для симуляции реальной работы
            val calculation = it * it + it / 2
        }

        // Имитация загрузки ресурсов
        loadCriticalResources()

        Log.d("MainActivity", "UI initialization completed")
    }

    /**
     * Загрузка критических ресурсов
     */
    private fun loadCriticalResources() {
        repeat(50) {
            val dummy = "Loading resource $it"
        }
        Log.d("MainActivity", "Critical resources loaded")
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "onStart - приложение становится видимым")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume - приложение активно")
    }
}

/**
 * Навигационный граф - важен для производительности
 */
@Composable
fun BaselineAppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "list"
    ) {
        composable("list") {
            ListScreen(
                onItemClick = { itemId ->
                    navController.navigate("detail/$itemId")
                }
            )
        }
        composable("detail/{itemId}") { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: "0"
            DetailScreen(
                itemId = itemId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}