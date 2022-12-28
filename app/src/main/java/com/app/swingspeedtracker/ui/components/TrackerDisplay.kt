package com.app.swingspeedtracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.swingspeedtracker.data.TrackerData

@Composable
fun TrackerDisplay(data: TrackerData?) {
    val clubSpeed = data?.clubSpeedMph ?: 0.0
    val headSpeed = data?.ballSpeedMph ?: 0.0
    val carry = data?.carry ?: 0.0
    val carryUnit = data?.carryUnit ?: ""

    Surface (
        color = Color.Black,
        shape = RectangleShape,
        modifier = Modifier.paddingFromBaseline(top = 30.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Column (
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TrackerDisplayRow("CLUB SPEED", clubSpeed, "MPH")
                TrackerDisplayRow("BALL SPEED", headSpeed, "MPH")
                TrackerDisplayRow("CARRY", carry, carryUnit)
            }
        }
    }
}

@Composable
fun TrackerDisplayRow(label: String, speed: Double, unit: String) {
    CardFrame(label) {
        Row {
            Text(
                text = String.format("%.2f", speed),
                fontWeight = FontWeight.SemiBold,
                style = typography.h1,
                modifier = Modifier.alignByBaseline()
            )
            Text(
                text = unit,
                style = typography.caption,
                modifier = Modifier.alignByBaseline()
            )
        }
    }
}