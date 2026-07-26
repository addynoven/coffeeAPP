package com.example.testing1.di

import com.cloudinary.Cloudinary
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CloudinaryModule {

    @Provides
    @Singleton
    fun provideCloudinary(): Cloudinary {
        // Updated with full credentials: API Key + API Secret + Cloud Name
        return Cloudinary("cloudinary://818269883432412:TWQzFg_c4N28mPs3g07qlC29HT8@dzao8h1ay") 
    }
}
