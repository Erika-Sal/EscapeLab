package com.example.escapelab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.escapelab.ui.theme.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Person
@Composable
fun VictoryScreen(onReturnHome: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("🎉", fontSize = 64.sp)
            Spacer(Modifier.height(24.dp))
            Text(
                "YOU ESCAPED",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 32.sp,
                color = AmberGlow,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "your team conquered the escape room",
                color = ParchmentDim,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(48.dp))
            Button(
                onClick = onReturnHome,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberMid,
                    contentColor = Color.Black
                )
            ) {
                Text(
                    "Return Home",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Black
                )
            }
        }
    }
}