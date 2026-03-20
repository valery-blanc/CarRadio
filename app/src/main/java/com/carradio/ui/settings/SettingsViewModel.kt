package com.carradio.ui.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carradio.data.repository.RadioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: RadioRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("carradio_prefs", Context.MODE_PRIVATE)

    private val _favoritesCount = MutableStateFlow(0)
    val favoritesCount: StateFlow<Int> = _favoritesCount.asStateFlow()

    var backgroundPlayback: Boolean
        get() = prefs.getBoolean("background_playback", true)
        set(value) { prefs.edit().putBoolean("background_playback", value).apply() }

    var screenAlwaysOn: Boolean
        get() = prefs.getBoolean("screen_always_on", true)
        set(value) { prefs.edit().putBoolean("screen_always_on", value).apply() }

    var preferredQuality: String
        get() = prefs.getString("preferred_quality", "normal") ?: "normal"
        set(value) { prefs.edit().putString("preferred_quality", value).apply() }

    var appLanguage: String
        get() = prefs.getString("app_language", "en") ?: "en"
        set(value) { prefs.edit().putString("app_language", value).apply() }

    init {
        viewModelScope.launch {
            _favoritesCount.value = repository.getFavoritesCount()
        }
    }
}
