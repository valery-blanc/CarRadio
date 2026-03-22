package com.carradio.di

import com.carradio.data.api.RadioBrowserApi
import com.carradio.data.api.RadioBrowserService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRadioBrowserApi(): RadioBrowserApi = RadioBrowserService.createApi()
}
