package com.example.tileterritory

import android.app.Application
import com.example.tileterritory.data.SettingsRepository

class TileTerritoryApp : Application() {
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
}
