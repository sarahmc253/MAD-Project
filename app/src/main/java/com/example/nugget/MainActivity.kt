package com.example.nugget

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

private class BarcodeImageAnalyzer(
    private val onBarcodeDetected: (Barcode) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        // Adjust formats as needed; you can also omit setBarcodeFormats to support “most formats”.
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_AZTEC,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_CODE_128
        )
        .enableAllPotentialBarcodes() // optional
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    // Simple debounce to prevent repeating the same Toast continuously
    private var lastValue: String? = null
    private var lastShownAtMs: Long = 0L

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNullOrEmpty()) return@addOnSuccessListener

                val first = barcodes.first()
                val value = first.rawValue ?: first.displayValue ?: "(no value)"

                val now = System.currentTimeMillis()
                val recentlyShown = (value == lastValue) && (now - lastShownAtMs < 1500)

                if (!recentlyShown) {
                    lastValue = value
                    lastShownAtMs = now
                    onBarcodeDetected(first)
                }
            }
            .addOnFailureListener {
                // You could log this; keep minimal for now.
            }
            .addOnCompleteListener {
                // Critical: close the frame, otherwise CameraX will stall
                imageProxy.close()
            }
    }
}

class MainActivity : AppCompatActivity() {

    private val cameraExecutor = Executors.newSingleThreadExecutor()

    private lateinit var loginView: LinearLayout
    private lateinit var previewView: PreviewView

    private val requestCodeCamera = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loginView = findViewById(R.id.loginView)
        previewView = findViewById(R.id.previewView)
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            // Fake login – always succeeds
            loginView.visibility = View.GONE
            previewView.visibility = View.VISIBLE

            ensureCameraPermissionAndStart()
        }
    }

    private fun ensureCameraPermissionAndStart() {
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

        if (granted) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                requestCodeCamera
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == requestCodeCamera) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (granted) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
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
                        val contextView = findViewById<View>(R.id.main)

                        if (value != "") {
                            Snackbar.make(contextView, value, LENGTH_INDEFINITE).show()
                            }
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview,
                analysis
            )
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
