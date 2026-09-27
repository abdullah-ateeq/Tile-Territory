package com.example.tileterritory.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tileterritory.game.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class BoardTheme { CLASSIC, MIDNIGHT }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val boardTheme: BoardTheme = BoardTheme.CLASSIC,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val showLegalMoves: Boolean = true,
    val showLastMove: Boolean = true
)

data class GameStatistics(
    val gamesPlayed: Int = 0,
    val player1Wins: Int = 0,
    val player2Wins: Int = 0,
    val draws: Int = 0,
    val highestTerritory: Int = 0
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tile_territory")

class SettingsRepository(context: Context) {

    private val store = context.applicationContext.dataStore

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val BOARD_THEME = stringPreferencesKey("board_theme")
        val SOUND = booleanPreferencesKey("sound_enabled")
        val HAPTICS = booleanPreferencesKey("haptics_enabled")
        val LEGAL = booleanPreferencesKey("show_legal_moves")
        val LAST = booleanPreferencesKey("show_last_move")
        val GAMES = intPreferencesKey("games_played")
        val P1_WINS = intPreferencesKey("player1_wins")
        val P2_WINS = intPreferencesKey("player2_wins")
        val DRAWS = intPreferencesKey("draws")
        val HIGHEST = intPreferencesKey("highest_territory")
    }

    private val preferences: Flow<Preferences> = store.data.catch { error ->
        if (error is IOException) emit(emptyPreferences()) else throw error
    }

    val settings: Flow<AppSettings> = preferences.map { p ->
        AppSettings(
            themeMode = p[Keys.THEME].toEnum(ThemeMode.SYSTEM),
            boardTheme = p[Keys.BOARD_THEME].toEnum(BoardTheme.CLASSIC),
            soundEnabled = p[Keys.SOUND] ?: true,
            hapticsEnabled = p[Keys.HAPTICS] ?: true,
            showLegalMoves = p[Keys.LEGAL] ?: true,
            showLastMove = p[Keys.LAST] ?: true
        )
    }

    val statistics: Flow<GameStatistics> = preferences.map { p ->
        GameStatistics(
            gamesPlayed = p[Keys.GAMES] ?: 0,
            player1Wins = p[Keys.P1_WINS] ?: 0,
            player2Wins = p[Keys.P2_WINS] ?: 0,
            draws = p[Keys.DRAWS] ?: 0,
            highestTerritory = p[Keys.HIGHEST] ?: 0
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) = store.edit { it[Keys.THEME] = mode.name }
    suspend fun setBoardTheme(theme: BoardTheme) = store.edit { it[Keys.BOARD_THEME] = theme.name }
    suspend fun setSoundEnabled(enabled: Boolean) = store.edit { it[Keys.SOUND] = enabled }
    suspend fun setHapticsEnabled(enabled: Boolean) = store.edit { it[Keys.HAPTICS] = enabled }
    suspend fun setShowLegalMoves(enabled: Boolean) = store.edit { it[Keys.LEGAL] = enabled }
    suspend fun setShowLastMove(enabled: Boolean) = store.edit { it[Keys.LAST] = enabled }

    suspend fun recordGame(winner: Player?, player1Score: Int, player2Score: Int) {
        store.edit { p ->
            p[Keys.GAMES] = (p[Keys.GAMES] ?: 0) + 1
            when (winner) {
                Player.PLAYER_ONE -> p[Keys.P1_WINS] = (p[Keys.P1_WINS] ?: 0) + 1
                Player.PLAYER_TWO -> p[Keys.P2_WINS] = (p[Keys.P2_WINS] ?: 0) + 1
                null -> p[Keys.DRAWS] = (p[Keys.DRAWS] ?: 0) + 1
            }
            p[Keys.HIGHEST] = maxOf(p[Keys.HIGHEST] ?: 0, player1Score, player2Score)
        }
    }

    suspend fun resetStatistics() {
        store.edit { p ->
            p.remove(Keys.GAMES)
            p.remove(Keys.P1_WINS)
            p.remove(Keys.P2_WINS)
            p.remove(Keys.DRAWS)
            p.remove(Keys.HIGHEST)
        }
    }

    private inline fun <reified T : Enum<T>> String?.toEnum(default: T): T =
        this?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default
}
