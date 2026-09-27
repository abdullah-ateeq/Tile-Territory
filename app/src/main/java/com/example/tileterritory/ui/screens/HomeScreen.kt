package com.example.tileterritory.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tileterritory.data.GameStatistics
import com.example.tileterritory.game.TileTerritoryRules
import com.example.tileterritory.ui.components.PrimaryButton
import com.example.tileterritory.ui.components.QuietButton
import com.example.tileterritory.ui.components.StaticBoard
import com.example.tileterritory.ui.components.ThinDivider

@Composable
fun HomeScreen(
    statistics: GameStatistics,
    onPlay: () -> Unit,
    onHowToPlay: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
        ) {
            val landscape = maxWidth > maxHeight && maxHeight < 600.dp
            if (landscape) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterHorizontally),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Emblem(Modifier.width(150.dp))
                        Spacer(Modifier.height(24.dp))
                        Title()
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Actions(onPlay, onHowToPlay, onSettings, onAbout)
                        Spacer(Modifier.height(20.dp))
                        StatsStrip(statistics)
                    }
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .widthIn(max = 420.dp)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 32.dp, vertical = 24.dp)
                    ) {
                        Emblem(Modifier.width(168.dp))
                        Spacer(Modifier.height(36.dp))
                        Title()
                        Spacer(Modifier.height(44.dp))
                        Actions(onPlay, onHowToPlay, onSettings, onAbout)
                        Spacer(Modifier.height(36.dp))
                        StatsStrip(statistics)
                    }
                }
            }
        }
    }
}

@Composable
private fun Emblem(modifier: Modifier) {
    StaticBoard(
        board = TileTerritoryRules.createInitialBoard(),
        legalMoves = TileTerritoryRules.initialState().legalMoves,
        modifier = modifier.semantics { contentDescription = "Tile Territory board" }
    )
}

@Composable
private fun Title() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "TILE TERRITORY",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(14.dp))
        ThinDivider(Modifier.width(48.dp))
        Spacer(Modifier.height(14.dp))
        Text(
            "Expand. Block. Claim the board.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Actions(onPlay: () -> Unit, onHowToPlay: () -> Unit, onSettings: () -> Unit, onAbout: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(max = 320.dp)
    ) {
        PrimaryButton("Play", onPlay, Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        QuietButton("How to Play", onHowToPlay)
        QuietButton("Settings", onSettings)
        QuietButton("About", onAbout)
    }
}

@Composable
private fun StatsStrip(statistics: GameStatistics) {
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .widthIn(max = 360.dp)
            .fillMaxWidth()
    ) {
        StatCell("Played", statistics.gamesPlayed)
        StatCell("P1 Wins", statistics.player1Wins)
        StatCell("P2 Wins", statistics.player2Wins)
        StatCell("Draws", statistics.draws)
    }
}

@Composable
private fun StatCell(label: String, value: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.semantics(mergeDescendants = true) {}
    ) {
        Text(value.toString(), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        Text(label.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
