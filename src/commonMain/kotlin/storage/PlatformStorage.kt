package storage

/**
 * Platform-specific storage abstraction for file operations.
 * This interface abstracts file system operations to allow for different implementations
 * across platforms (desktop, Android, etc.)
 */
interface PlatformStorage {
    
    /**
     * Saves properties to a file.
     * @param filename The name of the file (without path)
     * @param properties Map of key-value pairs to save
     */
    fun saveProperties(filename: String, properties: Map<String, String>)
    
    /**
     * Loads properties from a file.
     * @param filename The name of the file (without path)
     * @return Map of key-value pairs, empty if file doesn't exist
     */
    fun loadProperties(filename: String): Map<String, String>
    
    /**
     * Checks if a properties file exists.
     * @param filename The name of the file (without path)
     * @return True if the file exists, false otherwise
     */
    fun fileExists(filename: String): Boolean
    
    /**
     * Deletes a properties file.
     * @param filename The name of the file (without path)
     * @return True if deletion was successful, false otherwise
     */
    fun deleteFile(filename: String): Boolean
}