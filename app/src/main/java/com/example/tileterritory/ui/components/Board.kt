package com.example.tileterritory.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.tileterritory.game.Board
import com.example.tileterritory.game.GridPosition
import com.example.tileterritory.game.Player
import com.example.tileterritory.game.TileOwner
import com.example.tileterritory.game.TileTerritoryState
import com.example.tileterritory.ui.theme.BoardPalette
import com.example.tileterritory.ui.theme.LocalBoardPalette

/** A rejected tap on [position]; [token] changes on every tap so repeated taps re-flash. */
data class InvalidFlash(val position: GridPosition, val token: Int)

private const val CLAIM_FILL_MS = 160

@Composable
fun BoardFrame(
    modifier: Modifier = Modifier,
    framePadding: Dp = 6.dp,
    content: @Composable () -> Unit
) {
    val palette = LocalBoardPalette.current
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(palette.frame)
            .border(1.dp, palette.frameBorder, shape)
            .padding(framePadding)
    ) {
        content()
    }
}

@Composable
fun GameBoard(
    state: TileTerritoryState,
    showLegalMoves: Boolean,
    showLastMove: Boolean,
    invalidFlash: InvalidFlash?,
    inputEnabled: Boolean,
    onTileTap: (GridPosition) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalBoardPalette.current
    val hintColor = palette.markFor(state.currentPlayer).copy(alpha = 0.55f)
    BoardFrame(modifier) {
        Column(Modifier.fillMaxSize()) {
            state.board.forEachIndexed { r, row ->
                Row(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    row.forEachIndexed { c, owner ->
                        val position = GridPosition(r, c)
                        val legal = !state.isGameOver && position in state.legalMoves
                        BoardCell(
                            position = position,
                            owner = owner,
                            palette = palette,
                            showHint = showLegalMoves && legal && inputEnabled,
                            hintColor = hintColor,
                            isLegal = legal,
                            isLastMove = showLastMove && state.lastMove == position,
                            flashToken = invalidFlash?.takeIf { it.position == position }?.token,
                            onTap = { if (inputEnabled) onTileTap(position) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoardCell(
    position: GridPosition,
    owner: TileOwner,
    palette: BoardPalette,
    showHint: Boolean,
    hintColor: Color,
    isLegal: Boolean,
    isLastMove: Boolean,
    flashToken: Int?,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val owned = owner == TileOwner.PLAYER_ONE || owner == TileOwner.PLAYER_TWO
    // Keep the last owner so an undo can animate the territory back out.
    val paintOwner = remember { mutableStateOf(owner) }
    if (owned) paintOwner.value = owner
    val fill by animateFloatAsState(
        targetValue = if (owned) 1f else 0f,
        animationSpec = tween(CLAIM_FILL_MS, easing = FastOutSlowInEasing),
        label = "claimFill"
    )
    val flash = remember { Animatable(0f) }
    LaunchedEffect(flashToken) {
        if (flashToken != null) {
            flash.snapTo(0.9f)
            flash.animateTo(0f, tween(420))
        }
    }

    val description = cellDescription(position, owner, isLegal, isLastMove)
    Box(
        modifier = modifier
            .padding(2.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onTap
            )
            .semantics { contentDescription = description }
            .drawBehind {
                val drawOwner = when {
                    owner != TileOwner.EMPTY -> owner
                    paintOwner.value == TileOwner.BLOCKED -> TileOwner.EMPTY
                    else -> paintOwner.value
                }
                drawTile(drawOwner, palette, fill = if (owner == TileOwner.BLOCKED) 1f else fill)
                if (showHint && owner == TileOwner.EMPTY) drawLegalHint(hintColor)
                if (isLastMove && owned) drawInnerBorder(palette.lastMove.copy(alpha = 0.85f * fill))
                if (flash.value > 0f) drawInnerBorder(palette.invalid.copy(alpha = flash.value), widthFraction = 0.05f)
            }
    )
}

private fun cellDescription(position: GridPosition, owner: TileOwner, legal: Boolean, last: Boolean): String {
    val where = "row ${position.row + 1} column ${position.col + 1}"
    return when (owner) {
        TileOwner.PLAYER_ONE -> "Player 1 territory at $where" + if (last) ", last move" else ""
        TileOwner.PLAYER_TWO -> "Player 2 territory at $where" + if (last) ", last move" else ""
        TileOwner.BLOCKED -> "Blocked tile at $where"
        TileOwner.EMPTY -> if (legal) "Empty legal tile at $where" else "Empty tile at $where"
    }
}

/**
 * Non-interactive board drawn on a single Canvas — used for previews, the result summary and
 * the tutorial example. [fillFor] lets callers animate individual tiles.
 */
@Composable
fun StaticBoard(
    board: Board,
    modifier: Modifier = Modifier,
    legalMoves: Set<GridPosition> = emptySet(),
    legalPlayer: Player = Player.PLAYER_ONE,
    lastMove: GridPosition? = null,
    framePadding: Dp = 5.dp,
    fillFor: (GridPosition) -> Float = { 1f }
) {
    val palette = LocalBoardPalette.current
    BoardFrame(modifier, framePadding) {
        Canvas(Modifier.fillMaxSize()) {
            val rows = board.size
            val cols = board.firstOrNull()?.size ?: 0
            if (rows == 0 || cols == 0) return@Canvas
            val cellW = size.width / cols
            val cellH = size.height / rows
            val gap = cellW * 0.06f
            val tileSize = Size(cellW - gap * 2, cellH - gap * 2)
            board.forEachIndexed { r, row ->
                row.forEachIndexed { c, owner ->
                    val position = GridPosition(r, c)
                    val topLeft = Offset(c * cellW + gap, r * cellH + gap)
                    drawTile(owner, palette, topLeft, tileSize, fill = fillFor(position))
                    if (position in legalMoves && owner == TileOwner.EMPTY) {
                        drawLegalHint(palette.markFor(legalPlayer).copy(alpha = 0.6f), topLeft, tileSize)
                    }
                    if (position == lastMove) {
                        drawInnerBorder(palette.lastMove.copy(alpha = 0.85f), topLeft, tileSize)
                    }
                }
            }
        }
    }
}

/** Small tile swatch showing a player's colour and symbol. */
@Composable
fun PlayerBadge(player: Player, size: Dp, modifier: Modifier = Modifier) {
    val palette = LocalBoardPalette.current
    Canvas(modifier.size(size)) {
        drawTile(player.owner, palette)
    }
}
