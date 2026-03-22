package com.carradio.ui.timer

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carradio.player.PlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SleepTimerViewModel @Inject constructor(
    private val playerController: PlayerController
) : ViewModel() {

    private val _hours = MutableStateFlow(0)
    val hours: StateFlow<Int> = _hours.asStateFlow()

    private val _minutes = MutableStateFlow(0)
    val minutes: StateFlow<Int> = _minutes.asStateFlow()

    private val _seconds = MutableStateFlow(0)
    val seconds: StateFlow<Int> = _seconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _remainingSeconds = MutableStateFlow<Int?>(null)
    val remainingSeconds: StateFlow<Int?> = _remainingSeconds.asStateFlow()

    // MainActivity observes this to call finishAffinity() — avoids killProcess
    private val _shouldFinishApp = MutableStateFlow(false)
    val shouldFinishApp: StateFlow<Boolean> = _shouldFinishApp.asStateFlow()

    private var timerJob: Job? = null

    fun setHours(h: Int) { _hours.value = h.coerceIn(0, 23) }
    fun setMinutes(m: Int) { _minutes.value = m.coerceIn(0, 59) }
    fun setSeconds(s: Int) { _seconds.value = s.coerceIn(0, 59) }

    fun startTimer() {
        val total = _hours.value * 3600 + _minutes.value * 60 + _seconds.value
        if (total <= 0) return

        timerJob?.cancel()
        _isRunning.value = true
        _remainingSeconds.value = total

        timerJob = viewModelScope.launch {
            // Use elapsed real-time to avoid drift from delay() inaccuracies
            val endTime = SystemClock.elapsedRealtime() + total * 1000L

            while (true) {
                val remaining = ((endTime - SystemClock.elapsedRealtime()) / 1000L).toInt()
                if (remaining <= 0) break
                _remainingSeconds.value = remaining

                if (remaining <= 30) {
                    playerController.player.volume = remaining / 30f
                }
                delay(200L) // Poll frequently enough for accurate display
            }

            // Timer expired: stop playback and ask MainActivity to exit cleanly
            playerController.player.volume = 1f
            playerController.stop()
            _isRunning.value = false
            _remainingSeconds.value = null
            _shouldFinishApp.value = true
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        if (playerController.isPlayerInitialized) {
            playerController.player.volume = 1f
        }
        _isRunning.value = false
        _remainingSeconds.value = null
    }

    override fun onCleared() {
        super.onCleared()
        cancelTimer()
    }
}
