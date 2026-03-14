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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mad_project.ui.scanner.BarcodeImageAnalyzer
import com.example.mad_project.ui.theme.ShelfScanGreen
import com.example.mad_project.ui.theme.TextPrimary
import com.example.mad_project.ui.theme.TextSecondary
import com.example.mad_project.ui.viewmodel.AddItemViewModel
import java.util.concurrent.Executors

/**
 * Sources: CameraX (Preview + ImageAnalysis), bindToLifecycle(lifecycleOwner),
 * ActivityResultContracts.RequestPermission, Settings.ACTION_APPLICATION_DETAILS_SETTINGS.
 *
 * Add Item screen with live camera and barcode scanning. On scan, looks up product via
 * Open Food Facts and shows name/brand. Tap Add opens edit-details dialog (name, expiry, quantity);
 * Enter Manually opens the same dialog with empty name. Camera permission: if denied, show
 * Open Settings button to deep-link to app permissions. Callback passes (name, expiryDate, quantity)
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
    onAddScannedItem: (name: String, expiryDate: String?, quantity: Float) -> Unit,
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
            PermissionPlaceholder(
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
                onNameChange = viewModel::setDialogName,
                onExpiryChange = viewModel::setDialogExpiry,
                onQuantityChange = viewModel::setDialogQuantity,
                onConfirm = {
                    val (name, expiry, qty) = viewModel.getDialogValuesForAdd()
                    onAddScannedItem(name, expiry, qty)
                    viewModel.dismissDialog()
                },
                onDismiss = viewModel::dismissDialog
            )
        }
    }
}

@Composable
private fun PermissionPlaceholder(
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF9E9E9E), Color(0xFF757575), Color(0xFF616161))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Camera permission is needed to scan barcodes", color = Color.White, fontSize = 16.sp)
            Text(
                "If it was denied, enable it in Settings.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onRequestPermission, colors = ButtonDefaults.buttonColors(containerColor = ShelfScanGreen)) {
                Text("Grant permission")
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onOpenSettings,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Text("Open Settings")
            }
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
private fun BarcodeFrameHint() {
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
        Surface(shape = RoundedCornerShape(20.dp), color = Color.DarkGray.copy(alpha = 0.7f)) {
            Text(
                "Align barcode within the frame",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun ScannedItemCard(
    modifier: Modifier = Modifier,
    scannedBarcode: String,
    productName: String?,
    productBrand: String?,
    productLoading: Boolean,
    onRescan: () -> Unit,
    onAdd: () -> Unit
) {
    Card(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 100.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("ITEM SCANNED", color = ShelfScanGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Icon(Icons.Default.Check, contentDescription = null, tint = ShelfScanGreen, modifier = Modifier.size(14.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(48.dp).background(Color.LightGray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Keyboard, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        if (productLoading) {
                            Text("Looking up product…", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                            Text("Barcode: $scannedBarcode", color = TextSecondary, fontSize = 12.sp)
                        } else {
                            Text(
                                productName ?: "Barcode: $scannedBarcode",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 16.sp
                            )
                            Text(
                                productBrand?.let { "Brand: $it" } ?: "Add to inventory or enter details manually",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onRescan, modifier = Modifier.size(40.dp).background(Color.LightGray, CircleShape)) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Rescan", tint = Color.White)
                }
                Button(
                    onClick = onAdd,
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

@Composable
fun AddDetailsDialog(
    isManualEntry: Boolean,
    name: String,
    expiry: String,
    quantity: String,
    onNameChange: (String) -> Unit,
    onExpiryChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = colorScheme.onSurface,
        unfocusedTextColor = colorScheme.onSurface,
        focusedBorderColor = colorScheme.primary,
        unfocusedBorderColor = colorScheme.outline,
        cursorColor = colorScheme.primary,
        focusedLabelColor = colorScheme.primary,
        unfocusedLabelColor = colorScheme.onSurfaceVariant,
        focusedContainerColor = colorScheme.surface,
        unfocusedContainerColor = colorScheme.surface
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colorScheme.surfaceVariant,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                Icons.Default.Keyboard,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text = if (isManualEntry) "Enter item details" else "Add to list",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurfaceVariant
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Product name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )
                OutlinedTextField(
                    value = expiry,
                    onValueChange = onExpiryChange,
                    label = { Text("Expiry date (optional)") },
                    placeholder = { Text("YYYY-MM-DD", color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = onQuantityChange,
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = ShelfScanGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun AddItemScreenPreview() {
    AddItemScreen(onBackClick = {}, onEnterManuallyClick = {}, onAddScannedItem = { _, _, _ -> })
}
