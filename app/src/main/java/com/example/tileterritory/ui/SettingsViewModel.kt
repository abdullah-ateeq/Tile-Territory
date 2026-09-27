package com.example.tileterritory.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tileterritory.TileTerritoryApp
import com.example.tileterritory.data.AppSettings
import com.example.tileterritory.data.BoardTheme
import com.example.tileterritory.data.GameStatistics
import com.example.tileterritory.data.SettingsRepository
import com.example.tileterritory.data.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: SettingsRepository) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val statistics: StateFlow<GameStatistics> = repository.statistics
        .stateIn(viewModelScope, SharingStarted.Eagerly, GameStatistics())

    fun setThemeMode(mode: ThemeMode) = update { repository.setThemeMode(mode) }
    fun setBoardTheme(theme: BoardTheme) = update { repository.setBoardTheme(theme) }
    fun setSoundEnabled(enabled: Boolean) = update { repository.setSoundEnabled(enabled) }
    fun setHapticsEnabled(enabled: Boolean) = update { repository.setHapticsEnabled(enabled) }
    fun setShowLegalMoves(enabled: Boolean) = update { repository.setShowLegalMoves(enabled) }
    fun setShowLastMove(enabled: Boolean) = update { repository.setShowLastMove(enabled) }
    fun resetStatistics() = update { repository.resetStatistics() }

    private fun update(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TileTerritoryApp
                SettingsViewModel(app.settingsRepository)
            }
        }
    }
}
