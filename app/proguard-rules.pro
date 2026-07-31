# Essential Keep Rules for Coffee App

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Razorpay
-keep class com.razorpay.** {*;}
-dontwarn com.razorpay.**

# OkHttp / Ktor
-dontwarn okio.**
-dontwarn okhttp3.**

# Coil
-keep class coil.** { *; }

# Lottie
-keep class com.airbnb.lottie.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Hilt (usually handles itself, but being safe)
-keep class dagger.hilt.** { *; }
-keep class com.google.dagger.** { *; }
