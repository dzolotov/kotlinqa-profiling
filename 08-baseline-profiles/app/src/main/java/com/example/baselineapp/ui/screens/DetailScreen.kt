package com.example.baselineapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.baselineapp.model.DataItem
import com.example.baselineapp.repository.DataRepository

/**
 * Экран детализации - также важен для Baseline Profile
 * Часто открываемый экран после навигации из списка
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    itemId: String,
    onBackClick: () -> Unit
) {
    Log.d("DetailScreen", "Rendering DetailScreen for item: $itemId")

    var item by remember { mutableStateOf<DataItem?>(null) }

    // Загрузка детальной информации
    LaunchedEffect(itemId) {
        Log.d("DetailScreen", "Loading item details for: $itemId")
        item = DataRepository.loadItemDetails(itemId)

        // Симуляция дополнительной обработки данных
        performDetailProcessing()

        Log.d("DetailScreen", "Item details loaded")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item?.let { dataItem ->
                DetailCard(dataItem)

                // Дополнительная информация для демонстрации скроллинга
                repeat(10) { index ->
                    AdditionalInfoCard(index = index)
                }
            }
        }
    }
}

/**
 * Карточка с детальной информацией
 */
@Composable
fun DetailCard(item: DataItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "ID: ${item.id}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Дополнительные карточки для демонстрации скроллинга
 */
@Composable
fun AdditionalInfoCard(index: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = "Additional Info Section $index\n\nThis is some additional information that demonstrates scrolling behavior and helps test the performance of the detail screen.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Дополнительная обработка данных на экране детализации
 * Этот метод попадет в Baseline Profile
 */
private suspend fun performDetailProcessing() {
    Log.d("DetailScreen", "Performing detail processing...")

    // Симуляция обработки данных
    repeat(100) {
        val dummy = "Processing detail data iteration $it"
        val calculation = it * 2 + it / 3
    }

    Log.d("DetailScreen", "Detail processing completed")
}