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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mad_project.ui.scanner.BarcodeImageAnalyzer
import com.example.mad_project.ui.theme.ShelfScanGreen
import com.example.mad_project.ui.viewmodel.AddItemViewModel
import java.util.concurrent.Executors

/**
 * Sources: CameraX (Preview + ImageAnalysis), bindToLifecycle(lifecycleOwner),
 * ActivityResultContracts.RequestPermission, Settings.ACTION_APPLICATION_DETAILS_SETTINGS.
 *
 * Add Item screen with live camera and barcode scanning. On scan, looks up product via
 * Open Food Facts and shows name/brand. Tap Add opens edit-details dialog (name, expiry, quantity, location);
 * Enter Manually opens the same dialog with empty name. Camera permission: if denied, show
 * Open Settings button to deep-link to app permissions. Callback passes (name, expiryDate, quantity, location)
 * to add to pantry.
 *
 * Prompt: Wire Add Item to camera and Open Food Facts; add edit-details dialog before adding;
 * allow manual add; add Open Settings when camera permission denied.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    onBackClick: () -> Unit,
    onEnterManuallyClick: () -> Unit,
    onAddScannedItem: (name: String, expiryDate: String?, quantity: Float, location: String) -> Unit,
    viewModel: AddItemViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context) }

    var permissionRequestResult by remember { mutableStateOf<Boolean?>(null) }
    val hasCameraPermission = permissionRequestResult
        ?: (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> permissionRequestResult = granted }

    DisposableEffect(Unit) {
        onDispose { cameraExecutor.shutdown() }
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
                    if (value.isNotBlank()) viewModel.onBarcodeScanned(value)
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
        if (hasCameraPermission) {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        } else {
            CameraPermissionPlaceholder(
                onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                onOpenSettings = {
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    })
                }
            )
        }

        AddItemTopBar(onBackClick = onBackClick)

        if (hasCameraPermission && !uiState.hasScannedItem) {
            BarcodeFrameHint()
        }

        if (uiState.hasScannedItem && uiState.scannedBarcode != null) {
            ScannedItemCard(
                modifier = Modifier.align(Alignment.BottomCenter),
                scannedBarcode = uiState.scannedBarcode!!,
                productName = uiState.productInfo?.displayName,
                productBrand = uiState.productInfo?.brands,
                productLoading = uiState.productLoading,
                onRescan = viewModel::clearScan,
                onAdd = { viewModel.openAddDialog(manualEntry = false) }
            )
        }

        EnterManuallyButton(
            modifier = Modifier.align(Alignment.BottomCenter),
            onClick = { viewModel.openAddDialog(manualEntry = true) }
        )

        if (uiState.showEditDetailsDialog) {
            AddDetailsDialog(
                isManualEntry = uiState.openedFromManualEntry,
                name = uiState.dialogName,
                expiry = uiState.dialogExpiry,
                quantity = uiState.dialogQuantity,
                location = uiState.dialogLocation,
                onNameChange = viewModel::setDialogName,
                onExpiryChange = viewModel::setDialogExpiry,
                onQuantityChange = viewModel::setDialogQuantity,
                onLocationChange = viewModel::setDialogLocation,
                onConfirm = {
                    val values = viewModel.getDialogValuesForAdd()
                    onAddScannedItem(values.name, values.expiryDate, values.quantity, values.locationName)
                    viewModel.dismissDialog()
                },
                onDismiss = viewModel::dismissDialog
            )
        }
    }
}

@Composable
private fun AddItemTopBar(onBackClick: () -> Unit) {
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
            modifier = Modifier.size(40.dp).background(Color.DarkGray.copy(alpha = 0.7f), CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }
        Text("Add Item", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun EnterManuallyButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .navigationBarsPadding(),
        colors = ButtonDefaults.buttonColors(containerColor = ShelfScanGreen),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Default.Keyboard, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Enter Manually", color = Color.White)
    }
}

@Preview(showBackground = true)
@Composable
private fun AddItemScreenPreview() {
    AddItemScreen(onBackClick = {}, onEnterManuallyClick = {}, onAddScannedItem = { _, _, _, _ -> })
}
