package com.carradio.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carradio.data.db.FavoriteStation
import com.carradio.data.repository.RadioRepository
import com.carradio.domain.model.Country
import com.carradio.domain.model.RadioStation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: RadioRepository
) : ViewModel() {

    val favorites: StateFlow<List<FavoriteStation>> = repository.getFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _countriesState = MutableStateFlow<UiState<List<Country>>>(UiState.Loading)
    val countriesState: StateFlow<UiState<List<Country>>> = _countriesState.asStateFlow()

    private val _stationsState = MutableStateFlow<UiState<List<RadioStation>>>(UiState.Loading)
    val stationsState: StateFlow<UiState<List<RadioStation>>> = _stationsState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Featured countries shown first
    val featuredIsos = listOf("FR", "CH", "BE", "CA")

    fun loadCountries() {
        viewModelScope.launch {
            _countriesState.value = UiState.Loading
            try {
                val countries = repository.getCountries()
                _countriesState.value = UiState.Success(countries)
            } catch (e: Exception) {
                _countriesState.value = UiState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }

    fun loadStations(countryIso: String) {
        viewModelScope.launch {
            _stationsState.value = UiState.Loading
            try {
                val stations = repository.getStationsByCountryCode(countryIso)
                _stationsState.value = UiState.Success(stations)
            } catch (e: Exception) {
                _stationsState.value = UiState.Error(e.message ?: "Erreur inconnue")
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addFavorite(station: RadioStation, position: Int, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.addFavorite(station, position)
            onDone()
        }
    }

    fun removeFavorite(uuid: String) {
        viewModelScope.launch {
            repository.removeFavorite(uuid)
        }
    }

    fun removeAtPosition(position: Int) {
        viewModelScope.launch {
            repository.removeAtPosition(position)
        }
    }
}
