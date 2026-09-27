package com.example.tileterritory.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tileterritory.game.Board
import com.example.tileterritory.game.GridPosition
import com.example.tileterritory.game.TileOwner
import com.example.tileterritory.game.TileTerritoryRules
import com.example.tileterritory.ui.components.StaticBoard
import kotlinx.coroutines.delay
import kotlin.math.abs

private const val SPLASH_ANIMATION_MS = 1300
private const val SPLASH_HOLD_MS = 250L

/** The board split along the anti-diagonal: Player 1 lower-left, Player 2 upper-right. */
private val DividedBoard: Board = TileTerritoryRules.createInitialBoard().mapIndexed { r, row ->
    row.mapIndexed { c, owner ->
        when {
            owner == TileOwner.BLOCKED -> TileOwner.BLOCKED
            r > c -> TileOwner.PLAYER_ONE
            r < c -> TileOwner.PLAYER_TWO
            else -> TileOwner.EMPTY
        }
    }
}

private fun distanceFromStart(position: GridPosition): Int {
    val start = if (position.row > position.col) TileTerritoryRules.PLAYER_ONE_START else TileTerritoryRules.PLAYER_TWO_START
    return abs(position.row - start.row) + abs(position.col - start.col)
}

private val MaxDistance = DividedBoard.indices.maxOf { r ->
    DividedBoard[r].indices.maxOf { c -> distanceFromStart(GridPosition(r, c)) }
}.coerceAtLeast(1)

private fun ramp(value: Float, from: Float, to: Float) = ((value - from) / (to - from)).coerceIn(0f, 1f)

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }
    val finish by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(SPLASH_ANIMATION_MS, easing = LinearEasing))
        delay(SPLASH_HOLD_MS)
        finish()
    }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(36.dp),
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth()
            ) {
                val p = progress.value
                StaticBoard(
                    board = DividedBoard,
                    modifier = Modifier
                        .widthIn(max = 220.dp)
                        .fillMaxWidth(0.62f)
                        .graphicsLayer { alpha = ramp(p, 0f, 0.2f) },
                    fillFor = { position ->
                        val start = position == TileTerritoryRules.PLAYER_ONE_START ||
                            position == TileTerritoryRules.PLAYER_TWO_START
                        if (start) {
                            ramp(p, 0.15f, 0.3f)
                        } else {
                            val begin = 0.3f + 0.42f * distanceFromStart(position) / MaxDistance
                            ramp(p, begin, begin + 0.08f)
                        }
                    }
                )
                Text(
                    "TILE TERRITORY",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer {
                        val t = ramp(p, 0.72f, 0.95f)
                        alpha = t
                        translationY = (1f - t) * 12.dp.toPx()
                    }
                )
            }
        }
    }
}
