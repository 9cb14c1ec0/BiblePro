package resources

import java.io.InputStream

/**
 * Desktop implementation of ResourceLoader using JVM class loader.
 */
actual object ResourceLoader {
    actual fun loadResource(resourcePath: String): InputStream {
        val cleanPath = resourcePath.replace(" ", "")
        val fullPath = "/$cleanPath.xml"
        
        return ResourceLoader::class.java.getResourceAsStream(fullPath)
            ?: throw IllegalArgumentException("Resource not found: $resourcePath (tried: $fullPath)")
    }
    
    actual fun loadResourceWithExtension(resourcePath: String, extension: String): InputStream {
        val cleanPath = resourcePath.replace(" ", "")
        val fullPath = "/$cleanPath.$extension"
        
        return ResourceLoader::class.java.getResourceAsStream(fullPath)
            ?: throw IllegalArgumentException("Resource not found: $resourcePath with extension $extension (tried: $fullPath)")
    }
}