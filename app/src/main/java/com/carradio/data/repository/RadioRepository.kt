package com.carradio.data.repository

import com.carradio.data.db.FavoriteStation
import com.carradio.domain.model.Country
import com.carradio.domain.model.RadioStation
import kotlinx.coroutines.flow.Flow

interface RadioRepository {
    fun getFavorites(): Flow<List<FavoriteStation>>
    suspend fun addFavorite(station: RadioStation, position: Int)
    suspend fun removeFavorite(uuid: String)
    suspend fun removeAtPosition(position: Int)
    suspend fun getFavoritesCount(): Int
    suspend fun getCountries(): List<Country>
    suspend fun getStationsByCountryCode(countryCode: String): List<RadioStation>
    suspend fun notifyClick(stationUuid: String)
}
