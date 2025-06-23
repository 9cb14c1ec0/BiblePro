package storage

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

/**
 * Desktop implementation of PlatformStorage using the file system.
 * Stores files in the user's home directory with a .biblepro_ prefix.
 */
class DesktopPlatformStorage : PlatformStorage {
    
    private val storageDirectory = File(System.getProperty("user.home"))
    
    /**
     * Gets the full file path for a given filename.
     * @param filename The base filename
     * @return File object with full path
     */
    private fun getFile(filename: String): File {
        val prefixedFilename = if (filename.startsWith(".biblepro_")) {
            filename
        } else {
            ".biblepro_$filename"
        }
        return File(storageDirectory, prefixedFilename)
    }
    
    override fun saveProperties(filename: String, properties: Map<String, String>) {
        val file = getFile(filename)
        val props = Properties()
        
        properties.forEach { (key, value) ->
            props[key] = value
        }
        
        try {
            FileOutputStream(file).use { outputStream ->
                props.store(outputStream, "BiblePro Data")
            }
        } catch (e: Exception) {
            // Log error in a real implementation
            println("Error saving properties to $filename: ${e.message}")
        }
    }
    
    override fun loadProperties(filename: String): Map<String, String> {
        val file = getFile(filename)
        
        if (!file.exists()) {
            return emptyMap()
        }
        
        val props = Properties()
        val result = mutableMapOf<String, String>()
        
        try {
            FileInputStream(file).use { inputStream ->
                props.load(inputStream)
            }
            
            props.forEach { (key, value) ->
                result[key.toString()] = value.toString()
            }
        } catch (e: Exception) {
            // Log error in a real implementation
            println("Error loading properties from $filename: ${e.message}")
        }
        
        return result
    }
    
    override fun fileExists(filename: String): Boolean {
        return getFile(filename).exists()
    }
    
    override fun deleteFile(filename: String): Boolean {
        return try {
            getFile(filename).delete()
        } catch (e: Exception) {
            // Log error in a real implementation
            println("Error deleting file $filename: ${e.message}")
            false
        }
    }
}