package com.app.swingspeedtracker.ui.components

import androidx.compose.foundation.clickable
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
import com.app.swingspeedtracker.ui.UiEvent

@Composable
fun TrackerList(
    dataList: List<TrackerData>,
    onEvent: ((event: UiEvent) -> Unit)? = null
) {
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
                    items = dataList
                ) {
                    TrackerListItem(
                        data = it
                    ) {
                        if (onEvent != null) {
                            onEvent(UiEvent.ItemSelected(it))
                        }
                    }
                    Divider()
                }
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
fun TrackerListItem(
    data: TrackerData,
    onClick: (() -> Unit)? = null
) {
    val displayClubSpeed = data.clubSpeedMph ?: 0.0
    val displayBallSpeed = data.ballSpeedMph ?: 0.0
    val displayCarry = data.carry ?: 0.0
    val displayCarryUnit = data.carryUnit ?: ""
    val displaySmash = data.smashFactor ?: 0.0

    var backgroundColor = MaterialTheme.colors.background
    var color = MaterialTheme.colors.onBackground

    if (data.isSelected) {
        backgroundColor = MaterialTheme.colors.secondary
        color = MaterialTheme.colors.onSecondary
    }

    Card (
        shape = RectangleShape,
        backgroundColor = backgroundColor,
        contentColor = color,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (onClick != null) {
                    onClick()
                }
            }
    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
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