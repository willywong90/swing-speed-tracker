package com.app.swingspeedtracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
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
fun TrackerList(data: List<TrackerData>) {
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Column {
            Row(
                modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
            ) {
                TrackerHeader("CS (MPH)")
                TrackerHeader("BS (MPH)")
                TrackerHeader("CARRY")
                TrackerHeader("SMASH")
            }
            LazyColumn(
                modifier = Modifier.fillMaxHeight()
            ) {
                items(
                    items = data,
                    itemContent = {
                        TrackerListItem(data = it)
                        Divider()
                    })
            }
        }
    }
}

@Composable
fun RowScope.TrackerHeader(text: String) {
    Box (
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .align(Alignment.Center)
        )
    }
}

@Composable
fun TrackerListItem(data: TrackerData) {
    val displayClubSpeed = data.clubSpeedMph ?: 0.0
    val displayBallSpeed = data.ballSpeedMph ?: 0.0
    val displayCarry = data.carry ?: 0.0
    val displayCarryUnit = data.carryUnit ?: ""
    val displaySmash = data.smashFactor ?: 0.0

    Card (
        shape = RectangleShape,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrackerItemValue(value = String.format("%.2f", displayClubSpeed))
            TrackerItemValue(value = String.format("%.2f", displayBallSpeed))
            TrackerItemValue(value = String.format("%.1f $displayCarryUnit", displayCarry))
            TrackerItemValue(value = String.format("%.2f", displaySmash))
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
            style = MaterialTheme.typography.body1,
            modifier = Modifier
                .padding(10.dp)
                .align(Alignment.Center)
        )
    }
}