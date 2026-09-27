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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tileterritory.game.Player
import com.example.tileterritory.ui.GameUiState
import com.example.tileterritory.ui.components.PlayerBadge
import com.example.tileterritory.ui.components.PrimaryButton
import com.example.tileterritory.ui.components.SecondaryButton
import com.example.tileterritory.ui.components.StatRow
import com.example.tileterritory.ui.components.StaticBoard
import java.util.Locale
import kotlin.math.abs

@Composable
fun ResultScreen(ui: GameUiState, onPlayAgain: () -> Unit, onHome: () -> Unit) {
    val state = ui.game
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp)
        ) {
            val landscape = maxWidth > maxHeight * 1.1f
            val sideWidth = minOf(380.dp, maxWidth * 0.5f)
            val summary: @Composable () -> Unit = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Headline(ui)
                    Spacer(Modifier.height(24.dp))
                    StatRow("Player 1 Territory", state.player1Score.toString())
                    StatRow("Player 2 Territory", state.player2Score.toString())
                    StatRow("Unclaimed Cells", state.unclaimedCells.toString())
                    StatRow("Total Moves", state.moveCount.toString())
                    StatRow("Board Coverage", String.format(Locale.US, "%.0f%%", state.coveragePercent))
                    StatRow("Match Duration", formatDuration(ui.durationMillis))
                    Spacer(Modifier.height(28.dp))
                    PrimaryButton("Play Again", onPlayAgain, Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    SecondaryButton("Home", onHome, Modifier.fillMaxWidth())
                }
            }
            if (landscape) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {
                    StaticBoard(state.board, lastMove = state.lastMove, modifier = Modifier.weight(1f, fill = false))
                    Column(
                        Modifier
                            .width(sideWidth)
                            .verticalScroll(rememberScrollState())
                    ) { summary() }
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .widthIn(max = 480.dp)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Spacer(Modifier.height(8.dp))
                        StaticBoard(state.board, lastMove = state.lastMove, modifier = Modifier.width(180.dp))
                        Spacer(Modifier.height(28.dp))
                        summary()
                    }
                }
            }
        }
    }
}

@Composable
private fun Headline(ui: GameUiState) {
    val state = ui.game
    val winner = state.winner
    val title = when {
        winner == Player.PLAYER_ONE -> "PLAYER 1 WINS"
        winner == Player.PLAYER_TWO -> "PLAYER 2 WINS"
        else -> "DRAW"
    }
    val margin = abs(state.player1Score - state.player2Score)
    val subtitle = when {
        winner != null -> "by $margin tile" + if (margin == 1) "" else "s"
        else -> "Territory evenly divided"
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (winner != null) {
                PlayerBadge(winner, 26.dp)
                Spacer(Modifier.width(12.dp))
            }
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() }
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis.coerceAtLeast(0L) / 1000L)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
