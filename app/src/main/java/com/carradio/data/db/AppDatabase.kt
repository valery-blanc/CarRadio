package com.carradio.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteStation::class, CountryCache::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun countryCacheDao(): CountryCacheDao
}
