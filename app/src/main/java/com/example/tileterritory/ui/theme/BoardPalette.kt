package com.example.tileterritory.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.tileterritory.data.BoardTheme
import com.example.tileterritory.game.Player

/** Colours for the physical board. The board stays dark in both app themes, like a real game piece. */
@Immutable
data class BoardPalette(
    val frame: Color,
    val frameBorder: Color,
    val empty: Color,
    val emptyShadow: Color,
    val emptyHighlight: Color,
    val player1: Color,
    val player1Mark: Color,
    val player2: Color,
    val player2Mark: Color,
    val blocked: Color,
    val blockedHatch: Color,
    val lastMove: Color,
    val invalid: Color
) {
    fun fillFor(player: Player) = if (player == Player.PLAYER_ONE) player1 else player2
    fun markFor(player: Player) = if (player == Player.PLAYER_ONE) player1Mark else player2Mark
}

val ClassicBoard = BoardPalette(
    frame = Color(0xFF1F2328),
    frameBorder = Color(0x40B8955E),
    empty = Color(0xFF2B3037),
    emptyShadow = Color(0xFF181B1F),
    emptyHighlight = Color(0xFF3A4048),
    player1 = Color(0xFFA0503F),
    player1Mark = Color(0xFFE6B09A),
    player2 = Color(0xFF2F6F73),
    player2Mark = Color(0xFF9FD3CF),
    blocked = Color(0xFF5E5A54),
    blockedHatch = Color(0xFF79736A),
    lastMove = Color(0xFFF1E6D0),
    invalid = Color(0xFFC56B5F)
)

val MidnightBoard = BoardPalette(
    frame = Color(0xFF121827),
    frameBorder = Color(0x40B8955E),
    empty = Color(0xFF1C2436),
    emptyShadow = Color(0xFF0C111C),
    emptyHighlight = Color(0xFF2A3449),
    player1 = Color(0xFF8E3243),
    player1Mark = Color(0xFFE3A2AE),
    player2 = Color(0xFF2D5E94),
    player2Mark = Color(0xFFA7C6EA),
    blocked = Color(0xFF4A4D55),
    blockedHatch = Color(0xFF63676F),
    lastMove = Color(0xFFE9E4F2),
    invalid = Color(0xFFC56B5F)
)

fun BoardTheme.palette(): BoardPalette = when (this) {
    BoardTheme.CLASSIC -> ClassicBoard
    BoardTheme.MIDNIGHT -> MidnightBoard
}

val LocalBoardPalette = staticCompositionLocalOf { ClassicBoard }
