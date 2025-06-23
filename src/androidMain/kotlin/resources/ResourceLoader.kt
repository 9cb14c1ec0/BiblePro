package resources

import android.content.Context
import java.io.InputStream

/**
 * Android implementation of ResourceLoader using assets.
 * Requires application context to be initialized.
 */
actual object ResourceLoader {
    actual fun loadResource(resourcePath: String): InputStream {
        val context = getApplicationContext()
        val cleanPath = resourcePath.replace(" ", "")
        val fileName = "$cleanPath.xml"
        
        return try {
            context.assets.open(fileName)
        } catch (e: Exception) {
            throw IllegalArgumentException("Resource not found: $resourcePath (tried: $fileName)", e)
        }
    }
    
    actual fun loadResourceWithExtension(resourcePath: String, extension: String): InputStream {
        val context = getApplicationContext()
        val cleanPath = resourcePath.replace(" ", "")
        val fileName = "$cleanPath.$extension"
        
        return try {
            context.assets.open(fileName)
        } catch (e: Exception) {
            throw IllegalArgumentException("Resource not found: $resourcePath with extension $extension (tried: $fileName)", e)
        }
    }
}

// Global context holder for Android resource loading
private var applicationContext: Context? = null

/**
 * Initialize the resource loader with Android application context.
 * This should be called from the Application class or MainActivity.
 */
fun initializeResourceLoader(context: Context) {
    applicationContext = context.applicationContext
}

/**
 * Get the application context for resource loading.
 */
internal fun getApplicationContext(): Context {
    return applicationContext 
        ?: throw IllegalStateException("Resource loader not initialized. Call initializeResourceLoader() first.")
}