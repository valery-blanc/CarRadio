package com.carradio.ui.home

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carradio.data.db.FavoriteStation
import com.carradio.data.repository.RadioRepository
import com.carradio.domain.model.RadioStation
import com.carradio.player.PlayerController
import com.carradio.player.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PREF_FAVORITE_PAGE_COUNT = "favorite_page_count"
private const val SLOTS_PER_PAGE = 8

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: RadioRepository,
    val playerController: PlayerController,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("carradio_prefs", Context.MODE_PRIVATE)

    val favorites: StateFlow<List<FavoriteStation>> = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playerState: StateFlow<PlayerState> = playerController.state
    val currentStation: StateFlow<RadioStation?> = playerController.currentStation

    private val _favoritePageCount = MutableStateFlow(
        prefs.getInt(PREF_FAVORITE_PAGE_COUNT, 0)
    )
    val favoritePageCount: StateFlow<Int> = _favoritePageCount.asStateFlow()

    init {
        // BUG-2: auto-correct pageCount if favorites exist in DB but pageCount is 0
        // (happens after reinstall with Android Auto Backup restoring DB but not prefs)
        viewModelScope.launch {
            val existing = repository.getFavorites().first()
            if (existing.isNotEmpty()) {
                val neededPages = existing.maxOf { it.position / SLOTS_PER_PAGE } + 1
                if (_favoritePageCount.value < neededPages) {
                    saveFavoritePageCount(neededPages)
                }
            }
        }
    }

    fun onTileTapped(station: RadioStation) {
        viewModelScope.launch { repository.notifyClick(station.uuid) }
        playerController.togglePlayPause(station)
    }

    fun stopPlayback() {
        playerController.stop()
    }

    fun playStation(station: RadioStation) {
        viewModelScope.launch { repository.notifyClick(station.uuid) }
        playerController.play(station)
    }

    fun addFavoriteToNextAvailableSlot(station: RadioStation) {
        viewModelScope.launch {
            val currentFavorites = repository.getFavorites().first()
            val usedPositions = currentFavorites.map { it.position }.toSet()
            var pageCount = _favoritePageCount.value
            var targetPos = -1
            for (pos in 0 until pageCount * SLOTS_PER_PAGE) {
                if (pos !in usedPositions) {
                    targetPos = pos
                    break
                }
            }
            if (targetPos == -1) {
                pageCount++
                saveFavoritePageCount(pageCount)
                targetPos = (pageCount - 1) * SLOTS_PER_PAGE
            }
            repository.addFavorite(station, targetPos)
        }
    }

    fun addBlankPage() {
        saveFavoritePageCount(_favoritePageCount.value + 1)
    }

    fun removeFavoriteAtPosition(position: Int) {
        viewModelScope.launch { repository.removeAtPosition(position) }
    }

    fun swapFavorites(from: Int, to: Int) {
        viewModelScope.launch { repository.swapFavorites(from, to) }
    }

    fun removeFavorite(uuid: String) {
        viewModelScope.launch { repository.removeFavorite(uuid) }
    }

    private fun saveFavoritePageCount(count: Int) {
        _favoritePageCount.value = count
        prefs.edit().putInt(PREF_FAVORITE_PAGE_COUNT, count).apply()
    }
}
