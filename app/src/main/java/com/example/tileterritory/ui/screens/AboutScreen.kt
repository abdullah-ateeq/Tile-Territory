package com.example.tileterritory.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.example.tileterritory.game.TileTerritoryRules
import com.example.tileterritory.ui.components.SectionLabel
import com.example.tileterritory.ui.components.StaticBoard
import com.example.tileterritory.ui.components.SubScreen
import com.example.tileterritory.ui.components.ThinDivider

private val Features = listOf(
    "Local 2 Player",
    "8×8 Strategy Grid",
    "Connected Expansion",
    "Territory Scoring",
    "Offline Play"
)

@Composable
fun AboutScreen(onBack: () -> Unit) {
    SubScreen("About", onBack) {
        Spacer(Modifier.height(16.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            StaticBoard(TileTerritoryRules.createInitialBoard(), modifier = Modifier.width(120.dp))
        }
        Spacer(Modifier.height(24.dp))
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Tile Territory", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(4.dp))
            Text("Version 1.0", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "A polished local two-player strategy game where players expand connected territory and compete to control the most board spaces.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        SectionLabel("Features")
        Features.forEach { feature ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                val accent = MaterialTheme.colorScheme.primary
                Canvas(Modifier.size(8.dp)) {
                    drawRect(accent, topLeft = Offset(size.width * 0.15f, size.height * 0.15f), size = size * 0.7f)
                }
                Spacer(Modifier.width(16.dp))
                Text(feature, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
            }
            ThinDivider()
        }
    }
}
