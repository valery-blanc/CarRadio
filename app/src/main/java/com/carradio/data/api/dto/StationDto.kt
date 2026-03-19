package com.carradio.data.api.dto

import com.google.gson.annotations.SerializedName
import com.carradio.domain.model.RadioStation

data class StationDto(
    @SerializedName("stationuuid") val uuid: String,
    @SerializedName("name") val name: String,
    @SerializedName("url_resolved") val urlResolved: String?,
    @SerializedName("url") val url: String,
    @SerializedName("favicon") val favicon: String?,
    @SerializedName("country") val country: String,
    @SerializedName("countrycode") val countryCode: String,
    @SerializedName("codec") val codec: String,
    @SerializedName("bitrate") val bitrate: Int,
    @SerializedName("hls") val hls: Int,
    @SerializedName("votes") val votes: Int,
    @SerializedName("lastcheckok") val lastCheckOk: Int
) {
    fun toDomain(): RadioStation = RadioStation(
        uuid = uuid,
        name = name.trim(),
        streamUrl = if (!urlResolved.isNullOrBlank()) urlResolved else url,
        faviconUrl = favicon?.takeIf { it.isNotBlank() },
        country = country,
        countryCode = countryCode,
        codec = codec.uppercase(),
        bitrate = bitrate,
        isHls = hls == 1,
        votes = votes
    )
}
