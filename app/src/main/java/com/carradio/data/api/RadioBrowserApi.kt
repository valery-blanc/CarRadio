package com.carradio.data.api

import com.carradio.data.api.dto.CountryDto
import com.carradio.data.api.dto.StationDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RadioBrowserApi {

    @GET("json/countries")
    suspend fun getCountries(
        @Query("order") order: String = "name",
        @Query("reverse") reverse: Boolean = false,
        @Query("hidebroken") hideBroken: Boolean = true
    ): List<CountryDto>

    @GET("json/stations/search")
    suspend fun getStationsByCountryCode(
        @Query("countrycode") countryCode: String,
        @Query("order") order: String = "votes",
        @Query("reverse") reverse: Boolean = true,
        @Query("hidebroken") hideBroken: Boolean = true,
        @Query("limit") limit: Int = 200
    ): List<StationDto>

    @GET("json/stations/bycountryexact/{countryName}")
    suspend fun getStationsByCountryName(
        @Path("countryName") countryName: String,
        @Query("order") order: String = "votes",
        @Query("reverse") reverse: Boolean = true,
        @Query("hidebroken") hideBroken: Boolean = true,
        @Query("limit") limit: Int = 200
    ): List<StationDto>

    @GET("json/url/{stationUuid}")
    suspend fun notifyClick(
        @Path("stationUuid") stationUuid: String
    ): Any
}
