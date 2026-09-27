package com.example.tileterritory.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tileterritory.audio.SoundPlayer
import com.example.tileterritory.ui.GameViewModel
import com.example.tileterritory.ui.SettingsViewModel
import com.example.tileterritory.ui.screens.AboutScreen
import com.example.tileterritory.ui.screens.GameScreen
import com.example.tileterritory.ui.screens.HomeScreen
import com.example.tileterritory.ui.screens.HowToPlayScreen
import com.example.tileterritory.ui.screens.ResultScreen
import com.example.tileterritory.ui.screens.SettingsScreen
import com.example.tileterritory.ui.screens.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val GAME = "game"
    const val RESULT = "result"
    const val HOW_TO_PLAY = "how_to_play"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
}

@Composable
fun AppNavHost(settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val gameViewModel: GameViewModel = viewModel(factory = GameViewModel.Factory)
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val statistics by settingsViewModel.statistics.collectAsStateWithLifecycle()

    val soundPlayer = remember { SoundPlayer() }
    DisposableEffect(soundPlayer) { onDispose { soundPlayer.release() } }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn(tween(220)) },
        exitTransition = { fadeOut(tween(180)) },
        popEnterTransition = { fadeIn(tween(220)) },
        popExitTransition = { fadeOut(tween(180)) }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onFinished = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }
        composable(Routes.HOME) {
            HomeScreen(
                statistics = statistics,
                onPlay = {
                    gameViewModel.newGame()
                    navController.navigate(Routes.GAME) { launchSingleTop = true }
                },
                onHowToPlay = { navController.navigate(Routes.HOW_TO_PLAY) { launchSingleTop = true } },
                onSettings = { navController.navigate(Routes.SETTINGS) { launchSingleTop = true } },
                onAbout = { navController.navigate(Routes.ABOUT) { launchSingleTop = true } }
            )
        }
        composable(Routes.GAME) {
            GameScreen(
                viewModel = gameViewModel,
                settings = settings,
                soundPlayer = soundPlayer,
                onMenu = { navController.goHome() },
                onGameFinished = {
                    navController.navigate(Routes.RESULT) {
                        popUpTo(Routes.GAME) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.RESULT) {
            val ui by gameViewModel.uiState.collectAsStateWithLifecycle()
            if (!ui.game.isGameOver) {
                // No finished match to show (e.g. after process recreation).
                LaunchedEffect(Unit) { navController.goHome() }
            } else {
                ResultScreen(
                    ui = ui,
                    onPlayAgain = {
                        gameViewModel.newGame()
                        navController.navigate(Routes.GAME) {
                            popUpTo(Routes.RESULT) { inclusive = true }
                        }
                    },
                    onHome = { navController.goHome() }
                )
            }
        }
        composable(Routes.HOW_TO_PLAY) {
            HowToPlayScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                settings = settings,
                statistics = statistics,
                onThemeMode = settingsViewModel::setThemeMode,
                onBoardTheme = settingsViewModel::setBoardTheme,
                onSound = settingsViewModel::setSoundEnabled,
                onHaptics = settingsViewModel::setHapticsEnabled,
                onShowLegalMoves = settingsViewModel::setShowLegalMoves,
                onShowLastMove = settingsViewModel::setShowLastMove,
                onResetStatistics = settingsViewModel::resetStatistics,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}

private fun NavHostController.goHome() {
    if (!popBackStack(Routes.HOME, inclusive = false)) {
        navigate(Routes.HOME) {
            popUpTo(graph.id) { inclusive = true }
        }
    }
}
