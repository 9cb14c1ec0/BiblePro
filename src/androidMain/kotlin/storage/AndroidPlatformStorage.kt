package storage

import android.content.Context
import android.content.SharedPreferences

/**
 * Android implementation of PlatformStorage using SharedPreferences.
 * Stores data in Android's app-specific SharedPreferences.
 */
class AndroidPlatformStorage(private val context: Context) : PlatformStorage {
    
    /**
     * Gets SharedPreferences for a given filename.
     * @param filename The base filename (will be prefixed with "biblepro_")
     * @return SharedPreferences instance
     */
    private fun getSharedPreferences(filename: String): SharedPreferences {
        val prefName = if (filename.startsWith("biblepro_")) {
            filename
        } else {
            "biblepro_$filename"
        }
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
    }
    
    override fun saveProperties(filename: String, properties: Map<String, String>) {
        val prefs = getSharedPreferences(filename)
        val editor = prefs.edit()
        
        // Clear existing data first
        editor.clear()
        
        // Add all new properties
        properties.forEach { (key, value) ->
            editor.putString(key, value)
        }
        
        // Apply changes
        editor.apply()
    }
    
    override fun loadProperties(filename: String): Map<String, String> {
        val prefs = getSharedPreferences(filename)
        val result = mutableMapOf<String, String>()
        
        // Get all entries from SharedPreferences
        prefs.all.forEach { (key, value) ->
            if (value is String) {
                result[key] = value
            }
        }
        
        return result
    }
    
    override fun fileExists(filename: String): Boolean {
        val prefs = getSharedPreferences(filename)
        return prefs.all.isNotEmpty()
    }
    
    override fun deleteFile(filename: String): Boolean {
        return try {
            val prefs = getSharedPreferences(filename)
            prefs.edit().clear().apply()
            true
        } catch (e: Exception) {
            // Log error in a real implementation
            println("Error deleting SharedPreferences $filename: ${e.message}")
            false
        }
    }
}