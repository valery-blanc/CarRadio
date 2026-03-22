package com.carradio.data.repository

import com.carradio.data.api.RadioBrowserApi
import com.carradio.data.db.CountryCacheDao
import com.carradio.data.db.FavoriteDao
import com.carradio.data.db.FavoriteStation
import com.carradio.data.db.toCache
import com.carradio.data.db.toFavorite
import com.carradio.domain.model.Country
import com.carradio.domain.model.RadioStation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RadioRepositoryImpl @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val countryCacheDao: CountryCacheDao,
    private val api: RadioBrowserApi
) : RadioRepository {

    companion object {
        private const val CACHE_TTL_MS = 24 * 60 * 60 * 1000L // 24h
    }

    override fun getFavorites(): Flow<List<FavoriteStation>> =
        favoriteDao.getAllFavorites()

    override suspend fun addFavorite(station: RadioStation, position: Int) {
        favoriteDao.insertFavorite(station.toFavorite(position))
    }

    override suspend fun removeFavorite(uuid: String) {
        favoriteDao.deleteFavorite(uuid)
    }

    override suspend fun removeAtPosition(position: Int) {
        favoriteDao.deleteAtPosition(position)
    }

    // Delegates to the DAO @Transaction method for atomicity
    override suspend fun swapFavorites(fromPosition: Int, toPosition: Int) {
        favoriteDao.swapFavorites(fromPosition, toPosition)
    }

    override suspend fun removeFavoritePage(pageStart: Int, slotsPerPage: Int) {
        favoriteDao.removePage(pageStart, slotsPerPage)
    }

    override suspend fun getFavoritesCount(): Int =
        favoriteDao.count()

    override suspend fun getCountries(): List<Country> {
        val lastFetch = countryCacheDao.getLastFetchedAt() ?: 0L
        val cacheValid = (System.currentTimeMillis() - lastFetch) < CACHE_TTL_MS

        if (cacheValid) {
            val cached = countryCacheDao.getAllCountries()
            if (cached.isNotEmpty()) return cached.map { it.toDomain() }
        }

        val countries = api.getCountries()
            .filter { it.iso.isNotBlank() && it.stationCount > 0 }
            .map { it.toDomain() }

        // Atomic clear + insert via @Transaction DAO method
        countryCacheDao.replaceAll(countries.map { it.toCache() })
        return countries
    }

    override suspend fun getStationsByCountryCode(countryCode: String): List<RadioStation> =
        api.getStationsByCountryCode(countryCode).map { it.toDomain() }

    override suspend fun searchStationsByName(name: String): List<RadioStation> =
        api.searchStationsByName(name).map { it.toDomain() }

    override suspend fun getTagSuggestions(name: String): List<String> =
        api.getTagSuggestions(searchTerm = name).map { it.name }

    override suspend fun getStationsByTag(tag: String): List<RadioStation> =
        api.getStationsByTag(tag).map { it.toDomain() }

    override suspend fun notifyClick(stationUuid: String) {
        try {
            api.notifyClick(stationUuid)
        } catch (e: CancellationException) {
            throw e // Never swallow coroutine cancellation
        } catch (_: Exception) { }
    }
}
