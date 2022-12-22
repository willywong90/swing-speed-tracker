package com.app.swingspeedtracker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TrackerDisplay(data: TrackerData?) {
    val clubSpeed = data?.clubHeadSpeed ?: 0.0
    val headSpeed = data?.ballSpeed ?: 0.0
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
                TrackerInfoRow("CLUB SPEED", clubSpeed, "MPH")
                TrackerInfoRow("BALL SPEED", headSpeed, "MPH")
                TrackerInfoRow("CARRY", carry, carryUnit)
            }
        }
    }
}

@Composable
fun TrackerInfoRow(label: String, speed: Double, unit: String) {
    Card (
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
            .border(1.dp, Color.DarkGray, RoundedCornerShape(5.dp))
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text (
                color = MaterialTheme.colors.secondary,
                text = label,
                textAlign = TextAlign.Center,
                style = typography.body1,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray)
                    .padding(5.dp)
            )
            Row (
            ) {
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
}