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
        return Cloudinary(com.example.testing1.BuildConfig.CLOUDINARY_URL) 
    }
}
