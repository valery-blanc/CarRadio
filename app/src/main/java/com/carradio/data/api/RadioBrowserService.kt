package com.carradio.data.api

import android.util.Log
import com.carradio.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object RadioBrowserService {

    private const val FALLBACK_BASE_URL = "https://de1.api.radio-browser.info/"
    private const val FALLBACK_HOST = "de1.api.radio-browser.info"
    private const val DNS_LOOKUP_HOST = "all.api.radio-browser.info"
    private const val USER_AGENT = "CarRadio/1.0"
    private const val TAG = "RadioBrowserService"

    // Updated by background DNS thread; starts at fallback
    @Volatile private var resolvedHost: String = FALLBACK_HOST

    fun createApi(): RadioBrowserApi {
        // Resolve a real server in background — never blocks the calling thread
        Thread {
            try {
                val addresses = InetAddress.getAllByName(DNS_LOOKUP_HOST)
                if (addresses.isNotEmpty()) {
                    val candidate = addresses.random().canonicalHostName
                    // Reject raw IPs (e.g. "1.2.3.4") — HTTPS cert won't match
                    if (candidate.isNotBlank() &&
                        !candidate.matches(Regex("\\d+\\.\\d+\\.\\d+\\.\\d+"))
                    ) {
                        resolvedHost = candidate
                        if (BuildConfig.DEBUG) Log.d(TAG, "Resolved host: $candidate")
                    }
                }
            } catch (_: Exception) {
                // Keep fallback host — no crash, no ANR
            }
        }.apply { isDaemon = true }.start()

        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                    else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                // Redirect every request to the dynamically resolved host
                val original = chain.request()
                val newUrl = original.url.newBuilder().host(resolvedHost).build()
                chain.proceed(original.newBuilder().url(newUrl).build())
            }
            .addInterceptor(logging)
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", USER_AGENT)
                        .build()
                )
            }
            .build()

        return Retrofit.Builder()
            .baseUrl(FALLBACK_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RadioBrowserApi::class.java)
    }
}
