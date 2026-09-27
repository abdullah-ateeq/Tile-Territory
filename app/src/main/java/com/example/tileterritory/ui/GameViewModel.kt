package com.example.tileterritory.ui

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tileterritory.TileTerritoryApp
import com.example.tileterritory.data.SettingsRepository
import com.example.tileterritory.game.GameSession
import com.example.tileterritory.game.GridPosition
import com.example.tileterritory.game.Player
import com.example.tileterritory.game.TileTerritoryState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GameUiState(
    val game: TileTerritoryState,
    val canUndo: Boolean = false,
    /** Player currently being auto-passed; drives the "NO LEGAL MOVE" notice. */
    val passingPlayer: Player? = null,
    /** True once the final board has been shown and the result screen should open. */
    val readyForResult: Boolean = false,
    val durationMillis: Long = 0L
)

sealed interface GameEvent {
    data object Select : GameEvent
    data object Claimed : GameEvent
    data object Pass : GameEvent
    data class GameOver(val winner: Player?) : GameEvent
    data class InvalidTap(val position: GridPosition) : GameEvent
}

class GameViewModel(
    private val repository: SettingsRepository,
    private val clock: () -> Long = { SystemClock.elapsedRealtime() }
) : ViewModel() {

    private val session = GameSession()
    private var startedAt = clock()
    private var flowJob: Job? = null

    private val _uiState = MutableStateFlow(GameUiState(game = session.state))
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    fun newGame() {
        flowJob?.cancel()
        flowJob = null
        session.restart()
        startedAt = clock()
        _uiState.value = GameUiState(game = session.state)
    }

    fun onTileTapped(position: GridPosition) {
        val current = session.state
        if (current.isGameOver || current.isAnimating || flowJob?.isActive == true) return
        if (!session.claimTile(position)) {
            _events.tryEmit(GameEvent.InvalidTap(position))
            return
        }
        _events.tryEmit(GameEvent.Select)
        session.setAnimating(true)
        publish()
        flowJob = viewModelScope.launch {
            delay(CLAIM_ANIMATION_MS)
            _events.tryEmit(GameEvent.Claimed)
            resolveTurn()
        }
    }

    fun undo() {
        if (flowJob?.isActive == true || session.state.isGameOver) return
        if (session.undo()) publish()
    }

    /** Runs automatic passes and end-of-game handling, then unlocks input. */
    private suspend fun resolveTurn() {
        while (!session.state.isGameOver && session.state.legalMoves.isEmpty()) {
            publish(passingPlayer = session.state.currentPlayer)
            _events.tryEmit(GameEvent.Pass)
            delay(PASS_NOTICE_MS)
            session.handleNoMove()
        }
        if (session.state.isGameOver) {
            val finished = session.state
            val duration = clock() - startedAt
            publish(durationMillis = duration)
            _events.tryEmit(GameEvent.GameOver(finished.winner))
            viewModelScope.launch {
                repository.recordGame(finished.winner, finished.player1Score, finished.player2Score)
            }
            delay(RESULT_DELAY_MS)
            session.setAnimating(false)
            publish(durationMillis = duration, readyForResult = true)
        } else {
            session.setAnimating(false)
            publish()
        }
    }

    private fun publish(
        passingPlayer: Player? = null,
        durationMillis: Long = _uiState.value.durationMillis,
        readyForResult: Boolean = false
    ) {
        _uiState.value = GameUiState(
            game = session.state,
            canUndo = session.canUndo && !session.state.isGameOver,
            passingPlayer = passingPlayer,
            readyForResult = readyForResult,
            durationMillis = durationMillis
        )
    }

    companion object {
        const val CLAIM_ANIMATION_MS = 160L
        const val PASS_NOTICE_MS = 1100L
        const val RESULT_DELAY_MS = 1200L

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TileTerritoryApp
                GameViewModel(app.settingsRepository)
            }
        }
    }
}
