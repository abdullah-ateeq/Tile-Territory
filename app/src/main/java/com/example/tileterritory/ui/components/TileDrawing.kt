package com.example.tileterritory.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.lerp
import com.example.tileterritory.game.TileOwner
import com.example.tileterritory.ui.theme.BoardPalette

private fun cornerFor(size: Size) = CornerRadius(size.minDimension * 0.14f)

/**
 * Draws one board tile. [fill] (0..1) drives the claim animation: territory grows from the
 * centre of the recessed empty cell and fades in.
 */
fun DrawScope.drawTile(
    owner: TileOwner,
    palette: BoardPalette,
    topLeft: Offset = Offset.Zero,
    size: Size = this.size,
    fill: Float = 1f
) {
    val s = size.minDimension
    val corner = cornerFor(size)

    if (owner == TileOwner.BLOCKED) {
        drawBlocked(palette, topLeft, size, corner)
        return
    }

    // Recessed empty cell: darker top edge, faint lighter bottom edge.
    drawRoundRect(palette.empty, topLeft, size, corner)
    val edge = s * 0.045f
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(palette.emptyShadow, palette.emptyHighlight),
            startY = topLeft.y,
            endY = topLeft.y + size.height
        ),
        topLeft = topLeft + Offset(edge / 2, edge / 2),
        size = Size(size.width - edge, size.height - edge),
        cornerRadius = corner,
        style = Stroke(edge)
    )

    if (owner == TileOwner.EMPTY || fill <= 0f) return

    val base = if (owner == TileOwner.PLAYER_ONE) palette.player1 else palette.player2
    val mark = if (owner == TileOwner.PLAYER_ONE) palette.player1Mark else palette.player2Mark
    val scale = 0.55f + 0.45f * fill
    val w = size.width * scale
    val h = size.height * scale
    val origin = topLeft + Offset((size.width - w) / 2, (size.height - h) / 2)
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(lerp(base, Color.White, 0.07f), lerp(base, Color.Black, 0.14f)),
            startY = origin.y,
            endY = origin.y + h
        ),
        topLeft = origin,
        size = Size(w, h),
        cornerRadius = CornerRadius(corner.x * scale),
        alpha = fill
    )
    // Fine top highlight for depth.
    drawRoundRect(
        color = Color.White.copy(alpha = 0.07f * fill),
        topLeft = origin,
        size = Size(w, h),
        cornerRadius = CornerRadius(corner.x * scale),
        style = Stroke(s * 0.025f)
    )

    val center = topLeft + Offset(size.width / 2, size.height / 2)
    drawOwnerMark(owner, mark.copy(alpha = 0.62f * fill), center, s * scale)
}

/** Diamond for Player 1, ring for Player 2 — territory never relies on colour alone. */
fun DrawScope.drawOwnerMark(owner: TileOwner, color: Color, center: Offset, cellSize: Float) {
    val stroke = Stroke(width = cellSize * 0.05f)
    when (owner) {
        TileOwner.PLAYER_ONE -> {
            val r = cellSize * 0.15f
            val path = Path().apply {
                moveTo(center.x, center.y - r)
                lineTo(center.x + r, center.y)
                lineTo(center.x, center.y + r)
                lineTo(center.x - r, center.y)
                close()
            }
            drawPath(path, color, style = stroke)
        }
        TileOwner.PLAYER_TWO -> drawCircle(color, radius = cellSize * 0.115f, center = center, style = stroke)
        else -> Unit
    }
}

private fun DrawScope.drawBlocked(palette: BoardPalette, topLeft: Offset, size: Size, corner: CornerRadius) {
    val s = size.minDimension
    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(lerp(palette.blocked, Color.White, 0.05f), lerp(palette.blocked, Color.Black, 0.12f)),
            startY = topLeft.y,
            endY = topLeft.y + size.height
        ),
        topLeft = topLeft,
        size = size,
        cornerRadius = corner
    )
    val clip = Path().apply { addRoundRect(RoundRect(topLeft.x, topLeft.y, topLeft.x + size.width, topLeft.y + size.height, corner)) }
    clipPath(clip) {
        val step = s / 5f
        var x = -s
        while (x < s) {
            drawLine(
                color = palette.blockedHatch,
                start = Offset(topLeft.x + x, topLeft.y + size.height),
                end = Offset(topLeft.x + x + size.width, topLeft.y),
                strokeWidth = s * 0.035f
            )
            x += step
        }
    }
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.25f),
        topLeft = topLeft,
        size = size,
        cornerRadius = corner,
        style = Stroke(s * 0.03f)
    )
}

fun DrawScope.drawLegalHint(color: Color, topLeft: Offset = Offset.Zero, size: Size = this.size) {
    val s = size.minDimension
    drawCircle(
        color = color,
        radius = s * 0.075f,
        center = topLeft + Offset(size.width / 2, size.height / 2)
    )
}

fun DrawScope.drawInnerBorder(color: Color, topLeft: Offset = Offset.Zero, size: Size = this.size, widthFraction: Float = 0.04f) {
    val s = size.minDimension
    val inset = s * 0.1f
    drawRoundRect(
        color = color,
        topLeft = topLeft + Offset(inset, inset),
        size = Size(size.width - inset * 2, size.height - inset * 2),
        cornerRadius = CornerRadius(s * 0.1f),
        style = Stroke(s * widthFraction)
    )
}
