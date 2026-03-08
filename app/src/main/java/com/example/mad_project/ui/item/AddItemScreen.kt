package com.example.mad_project.ui.item

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.mad_project.data.OpenFoodFactsProduct
import com.example.mad_project.data.fetchProductByBarcode
import com.example.mad_project.ui.scanner.BarcodeImageAnalyzer
import com.example.mad_project.ui.theme.ShelfScanGreen
import com.example.mad_project.ui.theme.TextPrimary
import com.example.mad_project.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors

/**
 * Sources: CameraX (Preview + ImageAnalysis), bindToLifecycle(lifecycleOwner),
 * ActivityResultContracts.RequestPermission.
 *
 * Add Item screen with live camera and barcode scanning. 
 * On scan, looks up product
 * via Open Food Facts and shows name/brand. Handles camera permission and ties
 * the camera to the composable lifecycle.
 *
 * Prompt: Wire the Add Item screen to the camera scanner and Open Food Facts API
 * so scanned barcodes are resolved to product name and brand before adding.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    onBackClick: () -> Unit,
    onEnterManuallyClick: () -> Unit,
    onAddScannedItem: (name: String, expiryDate: String?, quantity: Float) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasScannedItem by remember { mutableStateOf(false) }
    var scannedBarcode by remember { mutableStateOf<String?>(null) }
    var productInfo by remember { mutableStateOf<OpenFoodFactsProduct?>(null) }
    var productLoading by remember { mutableStateOf(false) }
    var showEditDetailsDialog by remember { mutableStateOf(false) }
    var dialogName by remember { mutableStateOf("") }
    var dialogExpiry by remember { mutableStateOf("") }
    var dialogQuantity by remember { mutableStateOf("1") }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(showEditDetailsDialog) {
        if (showEditDetailsDialog) {
            dialogName = productInfo?.displayName ?: scannedBarcode ?: ""
            dialogExpiry = ""
            dialogQuantity = "1"
        }
    }
    val previewView = remember { PreviewView(context) }

    var permissionRequestResult by remember { mutableStateOf<Boolean?>(null) }
    val hasCameraPermission = permissionRequestResult
        ?: (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionRequestResult = granted
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) return@LaunchedEffect

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val cameraProvider = cameraProviderFuture.get()

        val preview = CameraPreview.Builder().build().apply {
            setSurfaceProvider(previewView.surfaceProvider)
        }

        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(cameraExecutor, BarcodeImageAnalyzer { barcode ->
                    val value = barcode.rawValue ?: barcode.displayValue ?: ""
                    if (value.isNotBlank()) {
                        scope.launch {
                            withContext(Dispatchers.Main.immediate) {
                                scannedBarcode = value
                                hasScannedItem = true
                                productInfo = null
                                productLoading = true
                            }
                            val product = fetchProductByBarcode(value)
                            withContext(Dispatchers.Main.immediate) {
                                productInfo = product
                                productLoading = false
                            }
                        }
                    }
                })
            }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analysis
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview or placeholder
        if (hasCameraPermission) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF9E9E9E),
                                Color(0xFF757575),
                                Color(0xFF616161)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Camera permission is needed to scan barcodes",
                    color = Color.White,
                    fontSize = 16.sp
                )
                Text(
                    "If it was denied, enable it in Settings.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = ShelfScanGreen)
                ) {
                    Text("Grant permission")
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Open Settings")
                }
            }
        }

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.DarkGray.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            Text(
                "Add Item",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.DarkGray.copy(alpha = 0.7f), CircleShape)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.White)
            }
        }

        // Center: barcode frame + instruction (only when camera is on and no scan yet)
        if (hasCameraPermission && !hasScannedItem) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(280.dp)
                        .height(160.dp)
                        .background(Color.Transparent)
                        .border(4.dp, ShelfScanGreen, RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.DarkGray.copy(alpha = 0.7f)
                ) {
                    Text(
                        "Align barcode within the frame",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Scanned item overlay card
        if (hasScannedItem && scannedBarcode != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 100.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "ITEM SCANNED",
                                color = ShelfScanGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = ShelfScanGreen,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.LightGray, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Keyboard,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                if (productLoading) {
                                    Text(
                                        "Looking up product…",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "Barcode: $scannedBarcode",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                } else {
                                    Text(
                                        productInfo?.displayName ?: "Barcode: $scannedBarcode",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        productInfo?.brands?.let { "Brand: $it" }
                                            ?: "Add to inventory or enter details manually",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = {
                                hasScannedItem = false
                                scannedBarcode = null
                                productInfo = null
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.LightGray, CircleShape)
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Rescan", tint = Color.White)
                        }
                        Button(
                            onClick = { showEditDetailsDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ShelfScanGreen),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !productLoading
                        ) {
                            Text("Add", color = Color.White)
                        }
                    }
                }
            }
        }

        // Enter Manually button at bottom
        Button(
            onClick = onEnterManuallyClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.Keyboard,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Enter Manually", color = Color.White)
        }

        // Edit details dialog (name, expiry, quantity) before adding
        if (showEditDetailsDialog) {
            AlertDialog(
                onDismissRequest = { showEditDetailsDialog = false },
                title = { Text("Add to list") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = dialogName,
                            onValueChange = { dialogName = it },
                            label = { Text("Product name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = dialogExpiry,
                            onValueChange = { dialogExpiry = it },
                            label = { Text("Expiry date (optional)") },
                            placeholder = { Text("YYYY-MM-DD") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = dialogQuantity,
                            onValueChange = { dialogQuantity = it.filter { c -> c.isDigit() || c == '.' } },
                            label = { Text("Quantity") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val name = dialogName.trim().ifBlank { "Scanned item" }
                            val expiry = dialogExpiry.trim().takeIf { it.isNotBlank() }
                            val quantity = dialogQuantity.toFloatOrNull() ?: 1f
                            onAddScannedItem(name, expiry, quantity)
                            showEditDetailsDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ShelfScanGreen)
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditDetailsDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddItemScreenPreview() {
    AddItemScreen(
        onBackClick = {},
        onEnterManuallyClick = {},
        onAddScannedItem = { _, _, _ -> }
    )
}
