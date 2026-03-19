package com.carradio.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.carradio.domain.model.RadioStation

@Entity(tableName = "favorites")
data class FavoriteStation(
    @PrimaryKey val uuid: String,
    val name: String,
    val streamUrl: String,
    val faviconUrl: String?,
    val country: String,
    val countryCode: String,
    val codec: String,
    val bitrate: Int,
    val isHls: Boolean,
    val position: Int,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): RadioStation = RadioStation(
        uuid = uuid,
        name = name,
        streamUrl = streamUrl,
        faviconUrl = faviconUrl,
        country = country,
        countryCode = countryCode,
        codec = codec,
        bitrate = bitrate,
        isHls = isHls,
        votes = 0
    )
}

fun RadioStation.toFavorite(position: Int): FavoriteStation = FavoriteStation(
    uuid = uuid,
    name = name,
    streamUrl = streamUrl,
    faviconUrl = faviconUrl,
    country = country,
    countryCode = countryCode,
    codec = codec,
    bitrate = bitrate,
    isHls = isHls,
    position = position
)
