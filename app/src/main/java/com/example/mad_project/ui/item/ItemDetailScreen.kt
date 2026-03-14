package com.example.mad_project.ui.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import com.example.mad_project.ui.components.LoadingView
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mad_project.data.InventoryItem
import com.example.mad_project.ui.theme.*
import com.example.mad_project.ui.viewmodel.ItemDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    itemId: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMarkConsumed: () -> Unit,
    onRemoveFromPantry: () -> Unit,
) {
    val viewModel: ItemDetailViewModel = viewModel(
        key = "item_detail_$itemId",
        factory = ItemDetailViewModel.factory(itemId)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val item = uiState.item

    if (uiState.isLoading) {
        LoadingView()
        return
    }
    val currentItem = item
    if (currentItem == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Item not found", color = TextSecondary)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "More options")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black,
                    actionIconContentColor = Color.Black
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Item image with overlay buttons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE0E0E0))
            ) {
                // Placeholder for image - could use Coil with currentItem.imageUrl
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Black)
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(44.dp)
                            .background(ExpiredRedLight, CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ExpiredRed)
                    }
                }
            }

            // Name + IN STOCK badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    currentItem.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ShelfScanGreen
                ) {
                    Text(
                        "IN STOCK",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            // Tags: Expiring in 5 days, Dairy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ExpiringSoonOrangeLight
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = ExpiringSoonOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            currentItem.expiryDisplay,
                            color = ExpiringSoonOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ChipBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            currentItem.category ?: currentItem.location.label,
                            color = TextPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // 2x2 detail cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailCard(
                    modifier = Modifier.weight(1f),
                    label = "EXPIRY DATE",
                    value = currentItem.expiryDate,
                    actionLabel = "Change",
                    actionIcon = Icons.Default.CalendarToday,
                    onClick = { }
                )
                DetailCard(
                    modifier = Modifier.weight(1f),
                    label = "QUANTITY",
                    value = currentItem.quantity,
                    actionLabel = "Adjust",
                    actionIcon = Icons.Default.Add,
                    onClick = { }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailCard(
                    modifier = Modifier.weight(1f),
                    label = "PURCHASED",
                    value = currentItem.purchasedDate ?: "—"
                )
                DetailCard(
                    modifier = Modifier.weight(1f),
                    label = "LOCATION",
                    value = currentItem.location.label
                )
            }

            // Notes
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 20.dp)
            ) {
                Text(
                    "NOTES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    currentItem.notes ?: "No notes.",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
            }

            // Action buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onMarkConsumed,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ShelfScanGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mark as Consumed", color = Color.White)
                }
                OutlinedButton(
                    onClick = onRemoveFromPantry,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Text("Remove from Pantry")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ItemDetailsScreenPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme(dynamicColour = false) {
        ItemDetailsScreen(
            itemId = "",
            onBackClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onMarkConsumed = {},
            onRemoveFromPantry = {}
        )
    }
}

@Composable
private fun DetailCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    actionLabel: String? = null,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                label,
                fontSize = 10.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (actionLabel != null && actionIcon != null && onClick != null) {
                TextButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        actionIcon,
                        contentDescription = null,
                        tint = ShelfScanGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(actionLabel, color = ShelfScanGreen, fontSize = 12.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailCardPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme(dynamicColour = false) {
        DetailCard(
            label = "Location",
            value = "Fridge",
            actionLabel = "Change",
            actionIcon = androidx.compose.material.icons.Icons.Default.Edit,
            onClick = {}
        )
    }
}
