package com.example.testing1.util

import com.cloudinary.Cloudinary
import com.cloudinary.transformation.resize.Resize
import com.cloudinary.transformation.delivery.Delivery
import com.cloudinary.transformation.delivery.Quality
import androidx.compose.runtime.staticCompositionLocalOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudinaryHelper @Inject constructor(
    private val cloudinary: Cloudinary
) {
    /**
     * Generates an optimized Cloudinary URL with specified width.
     * If the URL is not a Cloudinary URL, it returns the original.
     */
    fun optimize(url: String, width: Int = 500): String {
        if (!url.contains("cloudinary.com")) return url

        // Extract public ID from the URL
        // Example: https://res.cloudinary.com/demo/image/upload/sample.jpg -> sample
        val publicId = url.substringAfterLast("/").substringBeforeLast(".")
        
        return try {
            cloudinary.image {
                publicId(publicId)
                resize(Resize.fill {
                    width(width)
                })
                delivery(Delivery.quality(Quality.auto()))
            }.generate() ?: url
        } catch (e: Exception) {
            url
        }
    }
}

val LocalCloudinaryHelper = staticCompositionLocalOf<CloudinaryHelper> {
    error("No CloudinaryHelper provided")
}
