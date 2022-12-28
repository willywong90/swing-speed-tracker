package com.app.swingspeedtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.swingspeedtracker.data.TrackerData
import com.app.swingspeedtracker.ui.components.CardFrame
import com.app.swingspeedtracker.ui.components.TrackerList
import com.app.swingspeedtracker.ui.components.TrackerListItem

@Composable
fun HistoryScreen(
    dataList: List<TrackerData>,
    averageStats: TrackerData,
    clearHistory: () -> Unit
) {
    Surface (
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxHeight()
        ) {
            CardFrame("AVERAGES") {
                TrackerListItem(averageStats)
            }

            Button(
                enabled = dataList.isNotEmpty(),
                onClick = clearHistory,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.secondary,
                    disabledBackgroundColor = MaterialTheme.colors.secondary.copy(0.4f)
                ),
                modifier = Modifier
                    .padding(bottom = 10.dp)
            ) {
                Text(text = "Clear History")
            }

            TrackerList(dataList)
        }
    }
}