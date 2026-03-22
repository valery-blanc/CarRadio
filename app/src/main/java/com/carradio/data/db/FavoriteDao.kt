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

    // Atomic swap — both position updates run in a single DB transaction
    @Transaction
    suspend fun swapFavorites(fromPosition: Int, toPosition: Int) {
        val from = getAtPosition(fromPosition) ?: return
        val to = getAtPosition(toPosition)
        updatePosition(from.uuid, toPosition)
        if (to != null) updatePosition(to.uuid, fromPosition)
    }

    @Query("DELETE FROM favorites WHERE position >= :from AND position < :to")
    suspend fun deleteInRange(from: Int, to: Int)

    @Query("UPDATE favorites SET position = position - :shift WHERE position >= :from")
    suspend fun shiftPositionsDown(from: Int, shift: Int)

    // Atomic page removal: delete page slots then compact later pages
    @Transaction
    suspend fun removePage(pageStart: Int, slotsPerPage: Int) {
        deleteInRange(pageStart, pageStart + slotsPerPage)
        shiftPositionsDown(pageStart + slotsPerPage, slotsPerPage)
    }
}
