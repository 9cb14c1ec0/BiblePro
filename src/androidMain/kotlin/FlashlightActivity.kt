package com.oss.biblepro

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import theme.BibleProTheme

class FlashlightActivity : ComponentActivity() {
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    private var isFlashlightOn = false
    private var tapCount = 0
    private var lastTapTime = 0L

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Camera permission is required for flashlight", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        initializeCamera()
        checkCameraPermission()
        
        setContent {
            BibleProTheme {
                FlashlightScreen(
                    isFlashlightOn = isFlashlightOn,
                    onToggleFlashlight = { toggleFlashlight() },
                    onHiddenAccess = { checkHiddenAccess() }
                )
            }
        }
    }

    private fun initializeCamera() {
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = cameraManager?.cameraIdList?.get(0)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun toggleFlashlight() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            != PackageManager.PERMISSION_GRANTED) {
            checkCameraPermission()
            return
        }

        try {
            cameraId?.let { id ->
                cameraManager?.setTorchMode(id, !isFlashlightOn)
                isFlashlightOn = !isFlashlightOn
            }
        } catch (e: CameraAccessException) {
            Toast.makeText(this, "Error controlling flashlight", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkHiddenAccess() {
        val currentTime = System.currentTimeMillis()
        
        // Reset tap count if more than 3 seconds have passed
        if (currentTime - lastTapTime > 3000) {
            tapCount = 0
        }
        
        tapCount++
        lastTapTime = currentTime
        
        // Hidden access: 7 quick taps on the title
        if (tapCount >= 7) {
            tapCount = 0
            openBibleApp()
        }
    }

    private fun openBibleApp() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFlashlightOn) {
            try {
                cameraId?.let { id ->
                    cameraManager?.setTorchMode(id, false)
                }
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }
}

@Composable
fun FlashlightScreen(
    isFlashlightOn: Boolean,
    onToggleFlashlight: () -> Unit,
    onHiddenAccess: () -> Unit
) {
    val backgroundColor = if (isFlashlightOn) Color.White else Color.Black
    val contentColor = if (isFlashlightOn) Color.Black else Color.White
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App title (tap 7 times for hidden access)
        Text(
            text = "Flashlight",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier
                .padding(bottom = 48.dp)
                .pointerInput(Unit) {
                    detectTapGestures { onHiddenAccess() }
                }
        )
        
        // Flashlight toggle button
        Card(
            modifier = Modifier
                .size(200.dp),
            shape = CircleShape,
            elevation = 8.dp,
            backgroundColor = if (isFlashlightOn) Color.Yellow else Color.Gray
        ) {
            IconButton(
                onClick = onToggleFlashlight,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = if (isFlashlightOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = if (isFlashlightOn) "Turn off flashlight" else "Turn on flashlight",
                    modifier = Modifier.size(80.dp),
                    tint = if (isFlashlightOn) Color.Black else Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Status text
        Text(
            text = if (isFlashlightOn) "ON" else "OFF",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Tap to toggle flashlight",
            fontSize = 16.sp,
            color = contentColor.copy(alpha = 0.7f)
        )
    }
}