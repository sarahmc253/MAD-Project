package com.example.mad_project.ui.scanner

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * Sources: https://developers.google.com/ml-kit/vision/barcode-scanning/android,
 * CameraX ImageAnalysis/ImageProxy APIs.
 *
 * ImageAnalysis.Analyzer that detects barcodes from camera frames using ML Kit.
 * Debounces repeated detections (lastValue/lastShownAtMs, 1.5s); callback runs on
 * the camera executor thread so callers must switch to Main for UI/state updates.
 *
 * Prompt: Implement barcode scanning for the Add Item flow using ML Kit and CameraX.
 */
class BarcodeImageAnalyzer(
    private val onBarcodeDetected: (Barcode) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
            Barcode.FORMAT_AZTEC,
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39
        )
        .enableAllPotentialBarcodes()
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    private var lastValue: String? = null
    private var lastShownAtMs: Long = 0L

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
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
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
