package com.app.swingspeedtracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@Composable
fun TrackerList(data: MutableList<TrackerData>) {
    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Column {
            Row {
                TrackerHeader("CS (MPH)")
                TrackerHeader("BS (MPH)")
                TrackerHeader("CARRY")
                TrackerHeader("SMASH")
            }
            LazyColumn {
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