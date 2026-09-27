package com.example.tileterritory.game

/**
 * Holds the live match state plus snapshot history for undo.
 * Thin stateful wrapper around [TileTerritoryRules]; contains no timing or UI concerns.
 */
class GameSession(private val initial: TileTerritoryState = TileTerritoryRules.initialState()) {

    var state: TileTerritoryState = initial
        private set

    private val history = ArrayDeque<TileTerritoryState>()

    val canUndo: Boolean
        get() = history.isNotEmpty() && !state.isAnimating

    fun getNeighbors(position: GridPosition): List<GridPosition> =
        TileTerritoryRules.getNeighbors(position, state.board.size)

    fun getLegalMoves(player: Player): Set<GridPosition> =
        TileTerritoryRules.getLegalMoves(state.board, player)

    /** Returns true if the tile was claimed. The pre-move state is kept for undo. */
    fun claimTile(position: GridPosition): Boolean {
        val next = TileTerritoryRules.claimTile(state, position) ?: return false
        history.addLast(state)
        state = next
        return true
    }

    /** Resolves an automatic pass. Returns false if the current player still has a move. */
    fun handleNoMove(): Boolean {
        if (state.isGameOver || state.legalMoves.isNotEmpty()) return false
        state = TileTerritoryRules.handleNoMove(state)
        return true
    }

    /** Restores the snapshot taken before the most recent claim, including any passes it caused. */
    fun undo(): Boolean {
        if (state.isAnimating) return false
        val previous = history.removeLastOrNull() ?: return false
        state = previous
        return true
    }

    fun setAnimating(animating: Boolean) {
        state = state.copy(isAnimating = animating)
    }

    fun restart() {
        history.clear()
        state = initial
    }
}
