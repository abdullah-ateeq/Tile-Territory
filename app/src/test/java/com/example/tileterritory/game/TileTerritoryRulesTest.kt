package com.example.tileterritory.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TileTerritoryRulesTest {

    /** '.' empty, '1' player one, '2' player two, '#' blocked. */
    private fun board(vararg rows: String): Board = rows.map { row ->
        row.map {
            when (it) {
                '1' -> TileOwner.PLAYER_ONE
                '2' -> TileOwner.PLAYER_TWO
                '#' -> TileOwner.BLOCKED
                else -> TileOwner.EMPTY
            }
        }
    }

    private fun stateOf(board: Board, player: Player = Player.PLAYER_ONE, passes: Int = 0) =
        TileTerritoryState(
            board = board,
            currentPlayer = player,
            legalMoves = TileTerritoryRules.getLegalMoves(board, player),
            player1Score = TileTerritoryRules.countTiles(board, TileOwner.PLAYER_ONE),
            player2Score = TileTerritoryRules.countTiles(board, TileOwner.PLAYER_TWO),
            consecutivePasses = passes
        )

    @Test
    fun initialSetup() {
        val state = TileTerritoryRules.initialState()
        assertEquals(8, state.board.size)
        assertTrue(state.board.all { it.size == 8 })
        assertEquals(TileOwner.PLAYER_ONE, state.ownerAt(GridPosition(6, 1)))
        assertEquals(TileOwner.PLAYER_TWO, state.ownerAt(GridPosition(1, 6)))
        assertEquals(6, TileTerritoryRules.countTiles(state.board, TileOwner.BLOCKED))
        assertEquals(1, state.player1Score)
        assertEquals(1, state.player2Score)
        assertEquals(58, state.playableCells)
        assertEquals(Player.PLAYER_ONE, state.currentPlayer)
        assertFalse(state.isGameOver)
        // Blocked layout is symmetric under 180° rotation.
        TileTerritoryRules.BLOCKED_CELLS.forEach {
            assertTrue(GridPosition(7 - it.row, 7 - it.col) in TileTerritoryRules.BLOCKED_CELLS)
        }
        // Neither player starts trapped.
        assertEquals(4, TileTerritoryRules.getLegalMoves(state.board, Player.PLAYER_ONE).size)
        assertEquals(4, TileTerritoryRules.getLegalMoves(state.board, Player.PLAYER_TWO).size)
    }

    @Test
    fun neighborsAreOrthogonalAndInBounds() {
        assertEquals(
            setOf(GridPosition(2, 3), GridPosition(4, 3), GridPosition(3, 2), GridPosition(3, 4)),
            TileTerritoryRules.getNeighbors(GridPosition(3, 3)).toSet()
        )
        assertEquals(
            setOf(GridPosition(1, 0), GridPosition(0, 1)),
            TileTerritoryRules.getNeighbors(GridPosition(0, 0)).toSet()
        )
        assertEquals(3, TileTerritoryRules.getNeighbors(GridPosition(7, 4)).size)
    }

    @Test
    fun legalMoveGenerationMatchesConnectedEmptyCells() {
        val state = TileTerritoryRules.initialState()
        assertEquals(
            setOf(GridPosition(5, 1), GridPosition(7, 1), GridPosition(6, 0), GridPosition(6, 2)),
            state.legalMoves
        )
    }

    @Test
    fun connectedExpansionClaimsTileAndSwitchesPlayer() {
        val next = TileTerritoryRules.claimTile(TileTerritoryRules.initialState(), GridPosition(5, 1))!!
        assertEquals(TileOwner.PLAYER_ONE, next.ownerAt(GridPosition(5, 1)))
        assertEquals(Player.PLAYER_TWO, next.currentPlayer)
        assertEquals(GridPosition(5, 1), next.lastMove)
        assertEquals(1, next.moveCount)
        assertEquals(TileTerritoryRules.getLegalMoves(next.board, Player.PLAYER_TWO), next.legalMoves)
    }

    @Test
    fun scoreIncrementsOnClaim() {
        val next = TileTerritoryRules.claimTile(TileTerritoryRules.initialState(), GridPosition(6, 2))!!
        assertEquals(2, next.player1Score)
        assertEquals(1, next.player2Score)
        assertEquals(55, next.unclaimedCells)
    }

    @Test
    fun diagonalOnlyCellRejected() {
        val state = TileTerritoryRules.initialState()
        assertNull(TileTerritoryRules.claimTile(state, GridPosition(7, 2)))
        assertNull(TileTerritoryRules.claimTile(state, GridPosition(5, 0)))
    }

    @Test
    fun blockedCellRejected() {
        val state = stateOf(board("1#", ".."))
        assertFalse(GridPosition(0, 1) in state.legalMoves)
        assertNull(TileTerritoryRules.claimTile(state, GridPosition(0, 1)))
    }

    @Test
    fun opponentTileRejected() {
        val state = stateOf(board("12", ".."))
        assertNull(TileTerritoryRules.claimTile(state, GridPosition(0, 1)))
        assertEquals(setOf(GridPosition(1, 0)), state.legalMoves)
    }

    @Test
    fun sameTileCannotBeClaimedTwice() {
        val state = stateOf(board("1..", "...", "..2"))
        val afterP1 = TileTerritoryRules.claimTile(state, GridPosition(0, 1))!!
        val backToP1 = TileTerritoryRules.claimTile(afterP1, GridPosition(2, 1))!!
        assertNull(TileTerritoryRules.claimTile(backToP1, GridPosition(0, 1)))
    }

    @Test
    fun automaticPassSwitchesPlayer() {
        // Player 2 is sealed in the corner; Player 1 still has room.
        val state = stateOf(board("2#..", "#1..", "...."), Player.PLAYER_TWO)
        assertTrue(state.legalMoves.isEmpty())
        val passed = TileTerritoryRules.handleNoMove(state)
        assertEquals(Player.PLAYER_ONE, passed.currentPlayer)
        assertEquals(1, passed.consecutivePasses)
        assertFalse(passed.isGameOver)
        assertTrue(passed.legalMoves.isNotEmpty())
        // A successful claim resets the pass counter.
        val claimed = TileTerritoryRules.claimTile(passed, passed.legalMoves.first())!!
        assertEquals(0, claimed.consecutivePasses)
    }

    @Test
    fun manualPassRejectedWhenMoveExists() {
        val state = TileTerritoryRules.initialState()
        assertSame(state, TileTerritoryRules.handleNoMove(state))
    }

    @Test
    fun twoConsecutivePassesEndGame() {
        // Empty cell exists but neither player can reach it.
        val state = stateOf(board("1#.", "##.", "2#."), Player.PLAYER_ONE)
        val first = TileTerritoryRules.handleNoMove(state)
        assertFalse(first.isGameOver)
        val second = TileTerritoryRules.handleNoMove(first)
        assertTrue(second.isGameOver)
        assertEquals(2, second.consecutivePasses)
        assertTrue(second.isDraw)
    }

    @Test
    fun fullBoardEndsGameWithWinner() {
        val state = stateOf(board("11", "2."), Player.PLAYER_ONE)
        val end = TileTerritoryRules.claimTile(state, GridPosition(1, 1))!!
        assertTrue(end.isGameOver)
        assertEquals(Player.PLAYER_ONE, end.winner)
        assertFalse(end.isDraw)
        assertEquals(3, end.player1Score)
        assertEquals(100f, end.coveragePercent, 0.001f)
    }

    @Test
    fun winnerByTerritoryForPlayerTwo() {
        val end = TileTerritoryRules.finishGame(stateOf(board("122", "#2.")))
        assertEquals(Player.PLAYER_TWO, end.winner)
        assertEquals(3, end.player2Score)
    }

    @Test
    fun equalTerritoryIsDraw() {
        val end = TileTerritoryRules.finishGame(stateOf(board("12", "12")))
        assertTrue(end.isDraw)
        assertNull(end.winner)
    }

    @Test
    fun noActionsAfterGameOverOrDuringAnimation() {
        val over = TileTerritoryRules.finishGame(TileTerritoryRules.initialState())
        assertNull(TileTerritoryRules.claimTile(over, GridPosition(5, 1)))
        assertSame(over, TileTerritoryRules.handleNoMove(over))

        val animating = TileTerritoryRules.initialState().copy(isAnimating = true)
        assertNull(TileTerritoryRules.claimTile(animating, GridPosition(5, 1)))
    }

    @Test
    fun coverageHandlesAllBlockedBoard() {
        assertEquals(0f, stateOf(board("##")).copy(player1Score = 0, player2Score = 0).coveragePercent, 0f)
    }
}
