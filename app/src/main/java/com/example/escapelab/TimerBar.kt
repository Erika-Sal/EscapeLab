package com.example.escapelab

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.escapelab.ui.theme.*

@Composable
fun TimerBar(
    timeRemainingSeconds: Int?,
    totalSeconds: Int = 600
) {
    if (timeRemainingSeconds == null) return

    val progress = (timeRemainingSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f)

    val barColor by animateColorAsState(
        targetValue = when {
            timeRemainingSeconds <= 30 -> Color(0xFFE05555)
            timeRemainingSeconds <= 120 -> Color(0xFFE8A030)
            else -> Color(0xFF55E09A)
        },
        label = "timerColor"
    )

    val minutes = timeRemainingSeconds / 60
    val seconds = timeRemainingSeconds % 60
    val timeText = "%d:%02d".format(minutes, seconds)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "// TIME",
                style = MaterialTheme.typography.labelLarge,
                fontSize = 10.sp,
                color = AmberDim
            )
            Text(
                timeText,
                style = MaterialTheme.typography.labelLarge,
                fontSize = 16.sp,
                color = barColor
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = barColor,
            trackColor = AmberDim.copy(alpha = 0.2f)
        )
    }
}