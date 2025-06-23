package com.oss.biblepro

import App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import storage.initializePlatformStorage
import resources.initializeResourceLoader

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize platform storage with Android context
        initializePlatformStorage(this)
        
        // Initialize resource loader with Android context
        initializeResourceLoader(this)
        
        setContent {
            App()
        }
    }
}