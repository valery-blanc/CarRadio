package com.carradio.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carradio.data.db.FavoriteStation
import com.carradio.data.repository.RadioRepository
import com.carradio.domain.model.RadioStation
import com.carradio.player.PlayerController
import com.carradio.player.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RadioRepository,
    val playerController: PlayerController
) : ViewModel() {

    val favorites: StateFlow<List<FavoriteStation>> = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playerState: StateFlow<PlayerState> = playerController.state
    val currentStation: StateFlow<RadioStation?> = playerController.currentStation

    fun onTileTapped(station: RadioStation) {
        viewModelScope.launch {
            repository.notifyClick(station.uuid)
        }
        playerController.togglePlayPause(station)
    }

    fun stopPlayback() {
        playerController.stop()
    }
}
