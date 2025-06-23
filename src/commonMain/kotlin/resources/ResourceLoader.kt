package resources

import java.io.InputStream

/**
 * Platform-specific resource loader for Bible XML files.
 * Uses expect/actual pattern to handle different resource loading mechanisms
 * between desktop (JVM class loader) and Android (assets).
 */
expect object ResourceLoader {
    /**
     * Load a resource file as an InputStream.
     * @param resourcePath The path to the resource (without leading slash or .xml extension)
     * @return InputStream of the resource
     * @throws IllegalArgumentException if resource not found
     */
    fun loadResource(resourcePath: String): InputStream
    
    /**
     * Load a resource file with a specific extension as an InputStream.
     * @param resourcePath The path to the resource (without leading slash)
     * @param extension The file extension (e.g., "json", "txt")
     * @return InputStream of the resource
     * @throws IllegalArgumentException if resource not found
     */
    fun loadResourceWithExtension(resourcePath: String, extension: String): InputStream
}