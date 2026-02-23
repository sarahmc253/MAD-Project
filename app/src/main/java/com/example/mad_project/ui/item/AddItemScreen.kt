package com.example.mad_project.ui.item

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mad_project.ui.theme.ShelfScanGreen
import com.example.mad_project.ui.theme.TextPrimary
import com.example.mad_project.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    onBackClick: () -> Unit,
    onEnterManuallyClick: () -> Unit,
    onAddScannedItem: () -> Unit
) {
    var hasScannedItem by remember { mutableStateOf(true) } // set true to show overlay for demo
    val scannedItemName = "Greek Yogurt"
    val scannedItemExpiry = "10 Oct 2023"

    Box(modifier = Modifier.fillMaxSize()) {
        // Blurred camera background (placeholder)
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

        // Center: barcode frame + instruction
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

        // Scanned item overlay card
        if (hasScannedItem) {
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
                                Text(
                                    scannedItemName,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "Exp: $scannedItemExpiry",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { },
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.LightGray, CircleShape)
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Rescan", tint = Color.White)
                        }
                        Button(
                            onClick = onAddScannedItem,
                            colors = ButtonDefaults.buttonColors(containerColor = ShelfScanGreen),
                            shape = RoundedCornerShape(12.dp)
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
    }
}

@Preview(showBackground = true)
@Composable
private fun AddItemScreenPreview() {
    AddItemScreen(
        onBackClick = {},
        onEnterManuallyClick = {},
        onAddScannedItem = {}
    )
}
