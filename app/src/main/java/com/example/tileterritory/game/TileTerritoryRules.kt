package com.example.tileterritory.game

/**
 * Pure, UI-independent rules for Tile Territory.
 *
 * Every function takes a state (or board) and returns a new value; nothing is mutated.
 */
object TileTerritoryRules {

    const val BOARD_SIZE = 8

    val PLAYER_ONE_START = GridPosition(6, 1)
    val PLAYER_TWO_START = GridPosition(1, 6)

    /**
     * Six fixed obstacles, symmetric under 180° rotation and under reflection across the
     * main diagonal, so neither side is favoured by the layout.
     */
    val BLOCKED_CELLS: Set<GridPosition> = setOf(
        GridPosition(2, 2),
        GridPosition(5, 5),
        GridPosition(2, 5),
        GridPosition(5, 2),
        GridPosition(3, 4),
        GridPosition(4, 3)
    )

    fun createInitialBoard(): Board =
        List(BOARD_SIZE) { row ->
            List(BOARD_SIZE) { col ->
                val position = GridPosition(row, col)
                when (position) {
                    PLAYER_ONE_START -> TileOwner.PLAYER_ONE
                    PLAYER_TWO_START -> TileOwner.PLAYER_TWO
                    in BLOCKED_CELLS -> TileOwner.BLOCKED
                    else -> TileOwner.EMPTY
                }
            }
        }

    fun initialState(): TileTerritoryState {
        val board = createInitialBoard()
        return TileTerritoryState(
            board = board,
            currentPlayer = Player.PLAYER_ONE,
            legalMoves = getLegalMoves(board, Player.PLAYER_ONE),
            player1Score = countTiles(board, TileOwner.PLAYER_ONE),
            player2Score = countTiles(board, TileOwner.PLAYER_TWO)
        )
    }

    fun isInBounds(position: GridPosition, size: Int = BOARD_SIZE): Boolean =
        position.row in 0 until size && position.col in 0 until size

    /** Orthogonal neighbours only (up, down, left, right) that lie on the board. */
    fun getNeighbors(position: GridPosition, size: Int = BOARD_SIZE): List<GridPosition> =
        listOf(
            GridPosition(position.row - 1, position.col),
            GridPosition(position.row + 1, position.col),
            GridPosition(position.row, position.col - 1),
            GridPosition(position.row, position.col + 1)
        ).filter { isInBounds(it, size) }

    fun isLegalMove(board: Board, player: Player, position: GridPosition): Boolean {
        if (!isInBounds(position, board.size)) return false
        if (board[position.row][position.col] != TileOwner.EMPTY) return false
        return getNeighbors(position, board.size).any { board[it.row][it.col] == player.owner }
    }

    fun getLegalMoves(board: Board, player: Player): Set<GridPosition> {
        val moves = LinkedHashSet<GridPosition>()
        for (row in board.indices) {
            for (col in board[row].indices) {
                val position = GridPosition(row, col)
                if (isLegalMove(board, player, position)) moves += position
            }
        }
        return moves
    }

    fun countTiles(board: Board, owner: TileOwner): Int =
        board.sumOf { row -> row.count { it == owner } }

    fun isBoardComplete(board: Board): Boolean =
        board.none { row -> row.any { it == TileOwner.EMPTY } }

    /**
     * Claims [position] for the current player.
     *
     * Returns null (and leaves the game untouched) when the game is over, input is locked,
     * or the tile is not a legal connected expansion. The returned state has already
     * switched to the next player; if that player has no legal moves, the caller resolves
     * the pass with [handleNoMove].
     */
    fun claimTile(state: TileTerritoryState, position: GridPosition): TileTerritoryState? {
        if (state.isGameOver || state.isAnimating) return null
        if (position !in state.legalMoves) return null
        if (!isLegalMove(state.board, state.currentPlayer, position)) return null

        val board = state.board.mapIndexed { r, row ->
            if (r != position.row) row
            else row.mapIndexed { c, owner -> if (c == position.col) state.currentPlayer.owner else owner }
        }
        val claimed = state.copy(
            board = board,
            moveCount = state.moveCount + 1,
            lastMove = position,
            consecutivePasses = 0,
            player1Score = countTiles(board, TileOwner.PLAYER_ONE),
            player2Score = countTiles(board, TileOwner.PLAYER_TWO)
        )
        if (isBoardComplete(board)) return finishGame(claimed.copy(legalMoves = emptySet()))

        val next = state.currentPlayer.opponent
        return claimed.copy(
            currentPlayer = next,
            legalMoves = getLegalMoves(board, next)
        )
    }

    /**
     * Automatic pass for a player without legal moves. A second consecutive pass ends the
     * match. Does nothing if the current player can still move or the game is over.
     */
    fun handleNoMove(state: TileTerritoryState): TileTerritoryState {
        if (state.isGameOver || state.legalMoves.isNotEmpty()) return state

        val passes = state.consecutivePasses + 1
        if (passes >= 2) return finishGame(state.copy(consecutivePasses = passes))

        val next = state.currentPlayer.opponent
        return state.copy(
            consecutivePasses = passes,
            currentPlayer = next,
            legalMoves = getLegalMoves(state.board, next)
        )
    }

    fun finishGame(state: TileTerritoryState): TileTerritoryState {
        val p1 = countTiles(state.board, TileOwner.PLAYER_ONE)
        val p2 = countTiles(state.board, TileOwner.PLAYER_TWO)
        return state.copy(
            player1Score = p1,
            player2Score = p2,
            legalMoves = emptySet(),
            isGameOver = true,
            isDraw = p1 == p2,
            winner = when {
                p1 > p2 -> Player.PLAYER_ONE
                p2 > p1 -> Player.PLAYER_TWO
                else -> null
            }
        )
    }
}
