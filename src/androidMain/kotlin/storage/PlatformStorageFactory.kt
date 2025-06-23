package storage

import android.content.Context

// Global context holder for Android platform storage
private var applicationContext: Context? = null

/**
 * Initialize the platform storage with Android application context.
 * This should be called from the Application class or MainActivity.
 */
fun initializePlatformStorage(context: Context) {
    applicationContext = context.applicationContext
}

/**
 * Android implementation of the platform storage factory.
 * Returns an AndroidPlatformStorage instance using the application context.
 */
actual fun createPlatformStorage(): PlatformStorage {
    val context = applicationContext 
        ?: throw IllegalStateException("Platform storage not initialized. Call initializePlatformStorage() first.")
    return AndroidPlatformStorage(context)
}