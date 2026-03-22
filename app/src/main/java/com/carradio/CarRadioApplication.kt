package com.carradio

import android.app.Application
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CarRadioApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize AdMob on a background thread to avoid delaying app startup
        Thread {
            MobileAds.initialize(this) { /* Initialization complete — ads ready to load */ }
        }.apply { isDaemon = true }.start()
    }
}
