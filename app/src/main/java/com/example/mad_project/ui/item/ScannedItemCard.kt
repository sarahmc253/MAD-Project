package com.example.mad_project.ui.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mad_project.ui.theme.ShelfScanGreen
import com.example.mad_project.ui.theme.TextPrimary
import com.example.mad_project.ui.theme.TextSecondary

@Composable
internal fun ScannedItemCard(
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
