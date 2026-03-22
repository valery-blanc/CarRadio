package com.carradio.data.db

import androidx.room.*

@Dao
interface CountryCacheDao {

    @Query("SELECT * FROM countries_cache ORDER BY name ASC")
    suspend fun getAllCountries(): List<CountryCache>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(countries: List<CountryCache>)

    @Query("DELETE FROM countries_cache")
    suspend fun clearAll()

    @Query("SELECT lastFetchedAt FROM countries_cache LIMIT 1")
    suspend fun getLastFetchedAt(): Long?

    // Atomic cache refresh — clear + insert in a single DB transaction
    @Transaction
    suspend fun replaceAll(countries: List<CountryCache>) {
        clearAll()
        insertAll(countries)
    }
}
