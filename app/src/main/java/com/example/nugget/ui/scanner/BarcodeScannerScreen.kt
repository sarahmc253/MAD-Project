package com.example.nugget.ui.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.view.View
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.camera.view.PreviewView
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.Executors

@Composable
fun BarcodeScannerScreen(
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // Create the PreviewView inside Compose
    val previewView = remember { PreviewView(context) }

    // Clean up executor when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // Camera permission check (simple version)
    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

    if (!granted) {
        // In Compose you normally use rememberLauncherForActivityResult,
        // but to keep this minimal: show a message.
        LaunchedEffect(Unit) {
            Toast.makeText(context, "Camera permission not granted.", Toast.LENGTH_LONG).show()
            onDone()
        }
        return
    }

    // Start camera once when the composable enters composition
    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }

        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(cameraExecutor, BarcodeImageAnalyzer { barcode ->
                    val value = barcode.rawValue ?: barcode.displayValue ?: "(no value)"

                    if (value.isNotBlank()) {
                        Snackbar.make(
                            previewView as View,
                            value,
                            LENGTH_INDEFINITE
                        ).show()
                        // If you want to treat "first scan" as done:
                        // onDone()
                    }
                })
            }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            context as androidx.lifecycle.LifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analysis
        )
    }

    AndroidView(
        factory = { previewView }
    )
}
