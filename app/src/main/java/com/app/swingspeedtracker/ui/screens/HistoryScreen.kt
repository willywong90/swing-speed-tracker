package com.app.swingspeedtracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.swingspeedtracker.data.TrackerData
import com.app.swingspeedtracker.ui.UiEvent
import com.app.swingspeedtracker.ui.components.CardFrame
import com.app.swingspeedtracker.ui.components.StyledButton
import com.app.swingspeedtracker.ui.components.TrackerList
import com.app.swingspeedtracker.ui.components.TrackerListItem

@Composable
fun HistoryScreen(
    dataList: List<TrackerData>,
    averageStats: TrackerData,
    onEvent: (event: UiEvent) -> Unit
) {
    Surface (
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            CardFrame(
                label = "AVERAGES"
            ) {
                TrackerListItem(averageStats)
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ){
                StyledButton(
                    text = "Clear Selection",
                    onClick = { onEvent(UiEvent.ClearSelection) },
                    isEnabled = dataList.any { data -> data.isSelected }
                )

                StyledButton(
                    text = "Clear History",
                    onClick = { onEvent(UiEvent.ClearHistory) },
                    isEnabled = dataList.isNotEmpty()
                )
            }

            TrackerList(
                dataList = dataList,
                onEvent = onEvent
            )
        }
    }
}