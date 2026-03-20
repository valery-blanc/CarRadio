package com.carradio.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    fun applyLocale(context: Context, langCode: String): Context {
        val locale = Locale(langCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun getSavedLanguage(context: Context): String =
        context.getSharedPreferences("carradio_prefs", Context.MODE_PRIVATE)
            .getString("app_language", "en") ?: "en"
}
