package com.carradio.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY position ASC")
    fun getAllFavorites(): Flow<List<FavoriteStation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(station: FavoriteStation)

    @Query("DELETE FROM favorites WHERE uuid = :uuid")
    suspend fun deleteFavorite(uuid: String)

    @Query("DELETE FROM favorites WHERE position = :position")
    suspend fun deleteAtPosition(position: Int)

    @Query("SELECT COUNT(*) FROM favorites")
    suspend fun count(): Int

    @Query("SELECT * FROM favorites WHERE position = :position LIMIT 1")
    suspend fun getAtPosition(position: Int): FavoriteStation?

    @Query("UPDATE favorites SET position = :newPosition WHERE uuid = :uuid")
    suspend fun updatePosition(uuid: String, newPosition: Int)
}
