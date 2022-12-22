package com.app.swingspeedtracker

import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

@Composable
fun TrackerListItem(data: TrackerData) {
    Card (
        shape = RectangleShape,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrackerItemValue(value = String.format("%.2f", data.clubHeadSpeed))
            TrackerItemValue(value = String.format("%.2f", data.ballSpeed))
            TrackerItemValue(value = String.format("%.1f " + data.carryUnit, data.carry))
            TrackerItemValue(value = String.format("%.2f", data.smashFactor))
        }
    }
}

@Composable
fun RowScope.TrackerItemValue(value: String) {
    Box (
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
    ) {
        Text(
            text = value,
            style = typography.body1,
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.Center)
        )
    }
}