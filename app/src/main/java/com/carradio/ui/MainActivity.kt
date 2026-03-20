package com.carradio.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.carradio.ui.navigation.NavGraph
import com.carradio.ui.theme.CarRadioTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val dimHandler = Handler(Looper.getMainLooper())
    private val dimRunnable = Runnable { dimScreen() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            CarRadioTheme {
                NavGraph()
            }
        }
        scheduleDim()
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
        dimHandler.postDelayed(dimRunnable, DIM_DELAY_MS)
    }

    private fun dimScreen() {
        val params = window.attributes
        params.screenBrightness = MIN_BRIGHTNESS
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
