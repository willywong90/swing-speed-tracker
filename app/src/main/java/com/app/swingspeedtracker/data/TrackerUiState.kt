package com.app.swingspeedtracker.data

data class TrackerUiState (
    val isSensorConnected: Boolean = false,
    val sensorData: List<TrackerData> = listOf()
)

