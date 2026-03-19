package com.carradio.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress

object RadioBrowserService {

    private const val FALLBACK_BASE_URL = "https://de1.api.radio-browser.info/"
    private const val DNS_LOOKUP_HOST = "all.api.radio-browser.info"
    private const val USER_AGENT = "CarRadio/1.0"

    fun resolveBaseUrl(): String {
        return try {
            val addresses = InetAddress.getAllByName(DNS_LOOKUP_HOST)
            if (addresses.isNotEmpty()) {
                val host = addresses.random().hostName
                "https://$host/"
            } else {
                FALLBACK_BASE_URL
            }
        } catch (e: Exception) {
            FALLBACK_BASE_URL
        }
    }

    fun createApi(baseUrl: String = FALLBACK_BASE_URL): RadioBrowserApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RadioBrowserApi::class.java)
    }
}
