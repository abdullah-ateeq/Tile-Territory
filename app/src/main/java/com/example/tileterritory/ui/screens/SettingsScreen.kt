package com.example.tileterritory.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.tileterritory.data.AppSettings
import com.example.tileterritory.data.BoardTheme
import com.example.tileterritory.data.GameStatistics
import com.example.tileterritory.data.ThemeMode
import com.example.tileterritory.ui.components.SecondaryButton
import com.example.tileterritory.ui.components.SectionLabel
import com.example.tileterritory.ui.components.StatRow
import com.example.tileterritory.ui.components.SubScreen
import com.example.tileterritory.ui.components.ThinDivider

@Composable
fun SettingsScreen(
    settings: AppSettings,
    statistics: GameStatistics,
    onThemeMode: (ThemeMode) -> Unit,
    onBoardTheme: (BoardTheme) -> Unit,
    onSound: (Boolean) -> Unit,
    onHaptics: (Boolean) -> Unit,
    onShowLegalMoves: (Boolean) -> Unit,
    onShowLastMove: (Boolean) -> Unit,
    onResetStatistics: () -> Unit,
    onBack: () -> Unit
) {
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    SubScreen("Settings", onBack) {
        SectionLabel("Theme")
        Choice(
            options = ThemeMode.entries,
            selected = settings.themeMode,
            label = { it.name.lowercase().replaceFirstChar(Char::uppercase) },
            onSelect = onThemeMode
        )

        SectionLabel("Board Theme")
        Choice(
            options = BoardTheme.entries,
            selected = settings.boardTheme,
            label = { it.name.lowercase().replaceFirstChar(Char::uppercase) },
            onSelect = onBoardTheme
        )

        SectionLabel("Feedback")
        ToggleRow("Sound Effects", settings.soundEnabled, onSound)
        ToggleRow("Haptic Feedback", settings.hapticsEnabled, onHaptics)

        SectionLabel("Board")
        ToggleRow("Show Legal Moves", settings.showLegalMoves, onShowLegalMoves)
        ToggleRow("Show Last Move", settings.showLastMove, onShowLastMove)

        SectionLabel("Statistics")
        StatRow("Games Played", statistics.gamesPlayed.toString())
        StatRow("Player 1 Wins", statistics.player1Wins.toString())
        StatRow("Player 2 Wins", statistics.player2Wins.toString())
        StatRow("Draws", statistics.draws.toString())
        StatRow("Highest Territory", statistics.highestTerritory.toString())
        Spacer(Modifier.height(20.dp))
        SecondaryButton(
            "Reset Statistics",
            { confirmReset = true },
            Modifier.fillMaxWidth(),
            enabled = statistics.gamesPlayed > 0
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset statistics?", style = MaterialTheme.typography.headlineSmall) },
            text = { Text("All recorded results will be cleared.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    onResetStatistics()
                }) { Text("RESET", style = MaterialTheme.typography.labelLarge) }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Text("CANCEL", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
private fun <T> Choice(options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    val colors = MaterialTheme.colorScheme
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                border = BorderStroke(1.dp, colors.outline),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = colors.primary.copy(alpha = 0.18f),
                    activeContentColor = colors.onBackground,
                    inactiveContainerColor = colors.background,
                    inactiveContentColor = colors.onSurfaceVariant
                ),
                icon = {},
                label = { Text(label(option), style = MaterialTheme.typography.bodyMedium) }
            )
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
                .padding(vertical = 10.dp)
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
            Switch(
                checked = checked,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    uncheckedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
        ThinDivider()
    }
}
