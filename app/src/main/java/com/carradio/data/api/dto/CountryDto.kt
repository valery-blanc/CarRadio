package com.carradio.data.api.dto

import com.google.gson.annotations.SerializedName
import com.carradio.domain.model.Country

data class CountryDto(
    @SerializedName("name") val name: String,
    @SerializedName("iso_3166_1") val iso: String,
    @SerializedName("stationcount") val stationCount: Int
) {
    fun toDomain(): Country = Country(
        name = name,
        iso = iso,
        stationCount = stationCount
    )
}
