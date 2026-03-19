package com.carradio.domain.model

data class RadioStation(
    val uuid: String,
    val name: String,
    val streamUrl: String,
    val faviconUrl: String?,
    val country: String,
    val countryCode: String,
    val codec: String,
    val bitrate: Int,
    val isHls: Boolean,
    val votes: Int
)
