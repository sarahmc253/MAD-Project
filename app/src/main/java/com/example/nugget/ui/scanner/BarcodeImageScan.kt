package com.example.nugget.ui.scanner

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

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
            Barcode.FORMAT_CODE_128
        )
        .enableAllPotentialBarcodes()
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    private var lastValue: String? = null
    private var lastShownAtMs: Long = 0L

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
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
