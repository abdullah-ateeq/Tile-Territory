package com.example.tileterritory.game

enum class Player {
    PLAYER_ONE,
    PLAYER_TWO;

    val opponent: Player
        get() = if (this == PLAYER_ONE) PLAYER_TWO else PLAYER_ONE

    val owner: TileOwner
        get() = if (this == PLAYER_ONE) TileOwner.PLAYER_ONE else TileOwner.PLAYER_TWO

    val displayName: String
        get() = if (this == PLAYER_ONE) "Player 1" else "Player 2"
}

enum class TileOwner {
    EMPTY,
    PLAYER_ONE,
    PLAYER_TWO,
    BLOCKED
}

data class GridPosition(
    val row: Int,
    val col: Int
)

typealias Board = List<List<TileOwner>>

data class TileTerritoryState(
    val board: Board,
    val currentPlayer: Player = Player.PLAYER_ONE,
    val legalMoves: Set<GridPosition> = emptySet(),
    val player1Score: Int = 1,
    val player2Score: Int = 1,
    val lastMove: GridPosition? = null,
    val consecutivePasses: Int = 0,
    val moveCount: Int = 0,
    val winner: Player? = null,
    val isDraw: Boolean = false,
    val isGameOver: Boolean = false,
    val isAnimating: Boolean = false
) {
    val playableCells: Int
        get() = board.sumOf { row -> row.count { it != TileOwner.BLOCKED } }

    val unclaimedCells: Int
        get() = board.sumOf { row -> row.count { it == TileOwner.EMPTY } }

    /** Claimed playable cells / non-blocked cells × 100, safe for an empty board. */
    val coveragePercent: Float
        get() {
            val playable = playableCells
            if (playable == 0) return 0f
            return (player1Score + player2Score) * 100f / playable
        }

    fun ownerAt(position: GridPosition): TileOwner = board[position.row][position.col]

    fun scoreOf(player: Player): Int =
        if (player == Player.PLAYER_ONE) player1Score else player2Score
}
