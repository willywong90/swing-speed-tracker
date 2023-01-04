package com.app.swingspeedtracker.ui

import com.app.swingspeedtracker.data.TrackerData

sealed class UiEvent {
    data class ItemSelected(val selectedObject: TrackerData): UiEvent()
    object ClearHistory: UiEvent()
    object ClearSelection: UiEvent()
}