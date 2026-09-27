package com.example.tileterritory.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.tileterritory.game.Board
import com.example.tileterritory.game.Player
import com.example.tileterritory.game.TileOwner
import com.example.tileterritory.game.TileTerritoryRules
import com.example.tileterritory.ui.components.PlayerBadge
import com.example.tileterritory.ui.components.SectionLabel
import com.example.tileterritory.ui.components.StaticBoard
import com.example.tileterritory.ui.components.SubScreen

private val ExampleBoard: Board = listOf(
    "....",
    ".1#.",
    ".11.",
    "..2."
).map { row ->
    row.map {
        when (it) {
            '1' -> TileOwner.PLAYER_ONE
            '2' -> TileOwner.PLAYER_TWO
            '#' -> TileOwner.BLOCKED
            else -> TileOwner.EMPTY
        }
    }
}

private val Rules = listOf(
    "Each player begins with one territory tile.",
    "Claim one empty tile per turn.",
    "New tiles must touch your existing territory up, down, left or right — diagonals do not connect.",
    "Blocked stone cells can never be used.",
    "Opponent territory cannot be taken. Every claim is permanent.",
    "If you cannot move, your turn passes automatically.",
    "The match ends when the board is full or neither player can move. The player controlling more tiles wins."
)

@Composable
fun HowToPlayScreen(onBack: () -> Unit) {
    SubScreen("How to Play", onBack) {
        SectionLabel("Objective")
        Text(
            "Expand your connected territory, block your opponent's routes and control more of the board.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        SectionLabel("Rules")
        Rules.forEachIndexed { index, rule ->
            Row(Modifier.padding(vertical = 6.dp)) {
                Text(
                    "${index + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(28.dp)
                )
                Text(rule, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
            }
        }

        SectionLabel("Example")
        val legal = TileTerritoryRules.getLegalMoves(ExampleBoard, Player.PLAYER_ONE)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            StaticBoard(
                board = ExampleBoard,
                legalMoves = legal,
                legalPlayer = Player.PLAYER_ONE,
                modifier = Modifier
                    .width(200.dp)
                    .semantics {
                        contentDescription = "Example board: Player 1 owns three connected tiles. " +
                            "Dots mark the ${legal.size} empty tiles Player 1 may claim next. " +
                            "The stone tile and Player 2's tile cannot be claimed."
                    }
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Dots mark every tile Player 1 may claim. The stone cell and Player 2's tile are off limits, " +
                "and the diagonal corner does not connect.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        SectionLabel("Pieces")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Legend({ PlayerBadge(Player.PLAYER_ONE, 26.dp) }, "Player 1 — warm territory, diamond mark")
            Legend({ PlayerBadge(Player.PLAYER_TWO, 26.dp) }, "Player 2 — cool territory, ring mark")
            Legend({ StaticBoard(listOf(listOf(TileOwner.BLOCKED)), modifier = Modifier.width(26.dp), framePadding = 0.dp) }, "Blocked — stone, never scores")
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Enclosed empty areas are not awarded automatically — every tile must be claimed.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun Legend(icon: @Composable () -> Unit, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(Modifier.width(14.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}
