package com.carradio.di

import com.carradio.data.repository.RadioRepository
import com.carradio.data.repository.RadioRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindRadioRepository(impl: RadioRepositoryImpl): RadioRepository
}
