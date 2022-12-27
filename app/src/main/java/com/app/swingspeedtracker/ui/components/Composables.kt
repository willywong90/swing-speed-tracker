package com.app.swingspeedtracker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CardFrame(
    label: String,
    content: @Composable () -> Unit
) {
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
                style = MaterialTheme.typography.body1,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.DarkGray)
                    .padding(5.dp)
            )
            Box {
                content()
            }
        }
    }
}