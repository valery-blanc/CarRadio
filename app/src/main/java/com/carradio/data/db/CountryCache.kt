package com.carradio.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.carradio.domain.model.Country

@Entity(tableName = "countries_cache")
data class CountryCache(
    @PrimaryKey val iso: String,
    val name: String,
    val stationCount: Int,
    val lastFetchedAt: Long
) {
    fun toDomain(): Country = Country(name = name, iso = iso, stationCount = stationCount)
}

fun Country.toCache(): CountryCache = CountryCache(
    iso = iso,
    name = name,
    stationCount = stationCount,
    lastFetchedAt = System.currentTimeMillis()
)
