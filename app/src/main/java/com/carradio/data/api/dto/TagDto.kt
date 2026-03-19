package com.carradio.data.api.dto

import com.google.gson.annotations.SerializedName

data class TagDto(
    @SerializedName("name") val name: String,
    @SerializedName("stationcount") val stationCount: Int
)
