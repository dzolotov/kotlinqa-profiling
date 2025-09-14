package com.example.baselineapp.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.baselineapp.model.DataItem
import com.example.baselineapp.repository.DataRepository

/**
 * Экран со списком - критичен для производительности прокрутки
 * Этот экран попадет в Baseline Profile как часто используемый
 */
@Composable
fun ListScreen(
    onItemClick: (String) -> Unit
) {
    Log.d("ListScreen", "Rendering ListScreen")

    val items = remember { mutableStateListOf<DataItem>() }

    // Загрузка данных при первом показе экрана
    LaunchedEffect(Unit) {
        Log.d("ListScreen", "Loading data...")
        val loadedItems = DataRepository.loadItemList()
        items.addAll(loadedItems)
        Log.d("ListScreen", "Data loaded: ${items.size} items")
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Baseline Profile Demo",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        // LazyColumn - важный компонент для производительности
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                DataItemCard(
                    item = item,
                    onClick = { onItemClick(item.id) }
                )
            }
        }
    }
}

/**
 * Компонент элемента списка - часто рендерится
 * Попадет в Baseline Profile из-за частого использования
 */
@Composable
fun DataItemCard(
    item: DataItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}