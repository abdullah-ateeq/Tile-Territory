package com.example.tileterritory.ui.screens

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tileterritory.audio.SoundEffect
import com.example.tileterritory.audio.SoundPlayer
import com.example.tileterritory.data.AppSettings
import com.example.tileterritory.game.Player
import com.example.tileterritory.ui.GameEvent
import com.example.tileterritory.ui.GameUiState
import com.example.tileterritory.ui.GameViewModel
import com.example.tileterritory.ui.components.GameBoard
import com.example.tileterritory.ui.components.InvalidFlash
import com.example.tileterritory.ui.components.PlayerBadge
import com.example.tileterritory.ui.components.SecondaryButton
import com.example.tileterritory.ui.theme.LocalBoardPalette

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    settings: AppSettings,
    soundPlayer: SoundPlayer,
    onMenu: () -> Unit,
    onGameFinished: () -> Unit
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current
    val currentSettings by rememberUpdatedState(settings)
    var invalidFlash by remember { mutableStateOf<InvalidFlash?>(null) }
    var flashCounter by remember { mutableIntStateOf(0) }
    var showRestart by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val s = currentSettings
            when (event) {
                GameEvent.Select -> if (s.soundEnabled) soundPlayer.play(SoundEffect.SELECT)
                GameEvent.Claimed -> {
                    if (s.soundEnabled) soundPlayer.play(SoundEffect.CLAIM)
                    if (s.hapticsEnabled) view.haptic(HapticFeedbackConstants.CLOCK_TICK)
                }
                GameEvent.Pass -> {
                    if (s.soundEnabled) soundPlayer.play(SoundEffect.PASS)
                    if (s.hapticsEnabled) view.haptic(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                is GameEvent.GameOver -> {
                    if (s.soundEnabled) soundPlayer.play(SoundEffect.GAME_END)
                    if (s.hapticsEnabled) {
                        view.haptic(
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
                            else HapticFeedbackConstants.LONG_PRESS
                        )
                    }
                }
                is GameEvent.InvalidTap -> {
                    flashCounter++
                    invalidFlash = InvalidFlash(event.position, flashCounter)
                }
            }
        }
    }

    LaunchedEffect(ui.readyForResult) {
        if (ui.readyForResult) onGameFinished()
    }

    val inputEnabled = !ui.game.isAnimating && !ui.game.isGameOver
    val board: @Composable (Modifier) -> Unit = { modifier ->
        Box(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
            GameBoard(
                state = ui.game,
                showLegalMoves = settings.showLegalMoves,
                showLastMove = settings.showLastMove,
                invalidFlash = invalidFlash,
                inputEnabled = inputEnabled,
                onTileTap = viewModel::onTileTapped,
                modifier = Modifier.fillMaxSize()
            )
            PassNotice(ui.passingPlayer)
        }
    }
    val controls: @Composable (Modifier) -> Unit = { modifier ->
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = modifier) {
            SecondaryButton("Undo", viewModel::undo, Modifier.weight(1f), enabled = ui.canUndo)
            SecondaryButton("Restart", { showRestart = true }, Modifier.weight(1f), enabled = !ui.game.isGameOver)
            SecondaryButton("Menu", onMenu, Modifier.weight(1f))
        }
    }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(16.dp)
        ) {
            val sidePanelWidth = minOf(360.dp, maxWidth * 0.45f)
            if (maxWidth > maxHeight * 1.1f) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        board(Modifier)
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                        modifier = Modifier
                            .width(sidePanelWidth)
                            .verticalScroll(rememberScrollState())
                    ) {
                        PlayerPanel(Player.PLAYER_TWO, ui)
                        TurnStatus(ui, Modifier.fillMaxWidth())
                        PlayerPanel(Player.PLAYER_ONE, ui)
                        Spacer(Modifier.height(4.dp))
                        controls(Modifier.fillMaxWidth())
                    }
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .widthIn(max = 640.dp)
                            .fillMaxSize()
                    ) {
                        PlayerPanel(Player.PLAYER_TWO, ui)
                        TurnStatus(
                            ui,
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .fillMaxWidth()
                        ) {
                            board(Modifier)
                        }
                        Spacer(Modifier.height(14.dp))
                        PlayerPanel(Player.PLAYER_ONE, ui)
                        Spacer(Modifier.height(14.dp))
                        controls(Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }

    if (showRestart) {
        AlertDialog(
            onDismissRequest = { showRestart = false },
            title = { Text("Restart this match?", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("The current board will be cleared.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = {
                    showRestart = false
                    viewModel.newGame()
                }) { Text("RESTART", style = MaterialTheme.typography.labelLarge) }
            },
            dismissButton = {
                TextButton(onClick = { showRestart = false }) {
                    Text("CANCEL", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

private fun View.haptic(constant: Int) {
    performHapticFeedback(constant)
}

@Composable
private fun PlayerPanel(player: Player, ui: GameUiState, modifier: Modifier = Modifier) {
    val palette = LocalBoardPalette.current
    val state = ui.game
    val active = !state.isGameOver && state.currentPlayer == player
    val score = state.scoreOf(player)
    val borderColor by animateColorAsState(
        targetValue = if (active) palette.fillFor(player) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        animationSpec = tween(220),
        label = "panelBorder"
    )
    val shape = RoundedCornerShape(12.dp)
    val label = "${player.displayName} controls $score tiles" + if (active) ", to move" else ""
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(if (active) 1.5.dp else 1.dp, borderColor, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .clearAndSetSemantics { contentDescription = label }
    ) {
        PlayerBadge(player, 30.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                player.displayName.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = if (active) 1f else 0.6f)
            )
            Text(
                if (active) "TO MOVE" else "TERRITORY",
                style = MaterialTheme.typography.labelMedium,
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            score.toString(),
            fontSize = 30.sp,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = if (active) 1f else 0.7f)
        )
    }
}

@Composable
private fun TurnStatus(ui: GameUiState, modifier: Modifier = Modifier) {
    val state = ui.game
    val text = when {
        state.isGameOver -> "MATCH COMPLETE"
        ui.passingPlayer != null -> "NO LEGAL MOVE"
        else -> "${state.currentPlayer.displayName.uppercase()}'S TURN"
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .semantics { liveRegion = LiveRegionMode.Polite }
        ) {
            if (!state.isGameOver) {
                PlayerBadge(state.currentPlayer, 14.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            "UNCLAIMED ${state.unclaimedCells}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics { contentDescription = "${state.unclaimedCells} unclaimed tiles" }
        )
    }
}

@Composable
private fun PassNotice(passingPlayer: Player?) {
    var lastPlayer by remember { mutableStateOf(Player.PLAYER_ONE) }
    if (passingPlayer != null) lastPlayer = passingPlayer
    AnimatedVisibility(
        visible = passingPlayer != null,
        enter = fadeIn(tween(160)) + scaleIn(tween(160), initialScale = 0.96f),
        exit = fadeOut(tween(200)) + scaleOut(tween(200), targetScale = 0.98f)
    ) {
        val shape = RoundedCornerShape(12.dp)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(shape)
                .background(Color(0xE6101215))
                .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), shape)
                .padding(horizontal = 28.dp, vertical = 18.dp)
                .semantics { liveRegion = LiveRegionMode.Assertive }
        ) {
            Text("NO LEGAL MOVE", style = MaterialTheme.typography.titleMedium, color = Color(0xFFE8E4DC))
            Spacer(Modifier.height(4.dp))
            Text(
                "${lastPlayer.displayName} passes",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB9B4AA)
            )
        }
    }
}
