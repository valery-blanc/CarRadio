package com.carradio.ui.timer

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
            var remaining = total
            while (remaining > 0) {
                delay(1000L)
                remaining--
                _remainingSeconds.value = remaining

                if (remaining <= 30) {
                    playerController.player.volume = remaining / 30f
                }
            }
            // Timer expired: stop playback and exit app
            playerController.player.volume = 1f
            playerController.stop()
            _isRunning.value = false
            _remainingSeconds.value = null
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        playerController.player.volume = 1f
        _isRunning.value = false
        _remainingSeconds.value = null
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
