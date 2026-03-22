package com.carradio.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.carradio.player.PlayerController
import com.carradio.player.PlayerState
import com.carradio.ui.navigation.NavGraph
import com.carradio.ui.theme.CarRadioTheme
import com.carradio.ui.timer.SleepTimerViewModel
import com.carradio.util.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var playerController: PlayerController

    private val sleepTimerViewModel: SleepTimerViewModel by viewModels()

    private val dimHandler = Handler(Looper.getMainLooper())
    private val dimRunnable = Runnable { dimScreen() }

    // Single SharedPreferences instance — avoids repeated disk access per call
    private val prefs: SharedPreferences by lazy {
        getSharedPreferences("carradio_prefs", Context.MODE_PRIVATE)
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = LocaleHelper.getSavedLanguage(newBase)
        super.attachBaseContext(LocaleHelper.applyLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarRadioTheme {
                NavGraph()
            }
        }
        scheduleDim()

        // Observe timer expiry: exit the app cleanly via finishAffinity()
        // instead of the previous Process.killProcess() (CRITICAL fix)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sleepTimerViewModel.shouldFinishApp.collect { should ->
                    if (should) finishAffinity()
                }
            }
        }

        // Tie FLAG_KEEP_SCREEN_ON to playback state — only keep screen on when playing
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                playerController.state.collect { state ->
                    if (state == PlayerState.PLAYING || state == PlayerState.LOADING) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
            }
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        restoreBrightness()
        scheduleDim()
    }

    override fun onPause() {
        super.onPause()
        dimHandler.removeCallbacks(dimRunnable)
        restoreBrightness()
    }

    override fun onResume() {
        super.onResume()
        scheduleDim()
    }

    override fun onDestroy() {
        super.onDestroy()
        dimHandler.removeCallbacks(dimRunnable)
    }

    private fun scheduleDim() {
        dimHandler.removeCallbacks(dimRunnable)
        if (prefs.getBoolean("dim_enabled", true)) {
            dimHandler.postDelayed(dimRunnable, DIM_DELAY_MS)
        }
    }

    private fun dimScreen() {
        if (!prefs.getBoolean("dim_enabled", true)) return
        val brightness = prefs.getInt("dim_brightness", 10) / 100f
        val params = window.attributes
        params.screenBrightness = brightness.coerceAtLeast(MIN_BRIGHTNESS)
        window.attributes = params
    }

    private fun restoreBrightness() {
        val params = window.attributes
        params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        window.attributes = params
    }

    companion object {
        private const val DIM_DELAY_MS = 30_000L
        private const val MIN_BRIGHTNESS = 0.01f
    }
}
