package com.example.tileterritory.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSessionTest {

    @Test
    fun undoRestoresPreviousSnapshot() {
        val session = GameSession()
        val before = session.state
        assertFalse(session.canUndo)

        assertTrue(session.claimTile(GridPosition(5, 1)))
        assertTrue(session.claimTile(GridPosition(2, 6)))
        assertEquals(4, session.state.player1Score + session.state.player2Score)

        assertTrue(session.undo())
        assertEquals(Player.PLAYER_TWO, session.state.currentPlayer)
        assertEquals(TileOwner.EMPTY, session.state.ownerAt(GridPosition(2, 6)))
        assertEquals(GridPosition(5, 1), session.state.lastMove)

        assertTrue(session.undo())
        assertEquals(before, session.state)
        assertNull(session.state.lastMove)
        assertFalse(session.undo())
    }

    @Test
    fun undoBlockedWhileAnimating() {
        val session = GameSession()
        session.claimTile(GridPosition(5, 1))
        session.setAnimating(true)
        assertFalse(session.canUndo)
        assertFalse(session.undo())
        assertFalse(session.claimTile(GridPosition(2, 6)))
    }

    @Test
    fun invalidClaimDoesNotChangeState() {
        val session = GameSession()
        val before = session.state
        assertFalse(session.claimTile(GridPosition(0, 0)))
        assertFalse(session.claimTile(GridPosition(1, 6)))
        assertFalse(session.handleNoMove())
        assertEquals(before, session.state)
        assertFalse(session.canUndo)
    }

    @Test
    fun restartClearsHistory() {
        val session = GameSession()
        session.claimTile(GridPosition(5, 1))
        session.restart()
        assertEquals(TileTerritoryRules.initialState(), session.state)
        assertFalse(session.canUndo)
    }
}
