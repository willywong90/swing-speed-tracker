package com.app.swingspeedtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.app.swingspeedtracker.CardFrame
import com.app.swingspeedtracker.TrackerData
import com.app.swingspeedtracker.ui.components.TrackerDisplay
import com.app.swingspeedtracker.ui.components.TrackerList

@Composable
fun TrackerScreen(
    dataList: List<TrackerData>,
    bluetoothStatus: Boolean
) {
    val displayData = if (dataList.isNotEmpty()) dataList[0] else null
    val trimmedDataList = dataList.take(4)

    Surface (
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            BluetoothIndicator(bluetoothStatus)
            TrackerDisplay(displayData)
            CardFrame(label = "LAST 4") {
                TrackerList(trimmedDataList)
            }
        }
    }
}

@Composable
fun BluetoothIndicator(
    bluetoothStatus: Boolean
) {
    var btStatusColor = Color.DarkGray
    if (bluetoothStatus)
        btStatusColor = Color(0, 56, 224, 255)

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp, bottom = 1.dp)
        .height(5.dp)
        .background(btStatusColor)
    )
}