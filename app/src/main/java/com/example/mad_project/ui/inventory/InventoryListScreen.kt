package com.example.mad_project.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mad_project.data.ExpiryStatus
import com.example.mad_project.data.InventoryItem
import com.example.mad_project.data.PantryModel
import com.example.mad_project.ui.components.SyncLoadingOverlay
import com.example.mad_project.ui.item.AddDetailsDialog
import com.example.mad_project.data.ItemLocation
import com.example.mad_project.ui.theme.*
import com.example.mad_project.ui.viewmodel.InventoryViewModel

private enum class ViewMode { LIST, GRID }

private fun avatarColorsFor(name: String): Pair<Color, Color> {
    val palette = listOf(
        FridgeBlue to FridgeBlueText,
        PantryYellow to PantryYellowText,
        FreezerPurple to FreezerPurpleText,
        ShelfScanGreenLightBg to ShelfScanGreen,
        ExpiringSoonOrangeLight to ExpiringSoonOrange,
        ExpiredRedLight to ExpiredRed,
        Color(0xFFE8EAF6) to Color(0xFF3F51B5),
        Color(0xFFFCE4EC) to Color(0xFFE91E63),
    )
    val index = name.firstOrNull()?.code?.rem(palette.size)?.coerceAtLeast(0) ?: 0
    return palette[index]
}

private fun previewInventoryItem() = InventoryItem(
    id = "preview",
    name = "Preview Item",
    quantity = "1",
    location = ItemLocation.FRIDGE,
    expiryStatus = ExpiryStatus.OK,
    expiryDate = "2025-01-01",
    expiryDisplay = "Expires in 30 days"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryListScreen(
    onItemClick: (String) -> Unit,
    onScannerClick: () -> Unit,
    viewModel: InventoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var statusFilter by remember { mutableStateOf("All") }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    var showAddDialog by remember { mutableStateOf(false) }
    var addDialogName by remember { mutableStateOf("") }
    var addDialogExpiry by remember { mutableStateOf("") }
    var addDialogQuantity by remember { mutableStateOf("1") }
    var addDialogLocation by remember { mutableStateOf(ItemLocation.PANTRY) }
    val filteredItems = uiState.items
        .filter { it.name.contains(searchQuery, ignoreCase = true) }
        .filter { selectedCategory == "All" || it.location.name == selectedCategory.uppercase() }
        .filter { statusFilter == "All" || it.expiryStatus.label == statusFilter }

    val expiringSoon = uiState.items.count { it.expiryStatus == ExpiryStatus.EXPIRES_SOON }
    val expired = uiState.items.count { it.expiryStatus == ExpiryStatus.EXPIRED }
    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        topBar = { },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddDialog = true
                    addDialogName = ""
                    addDialogExpiry = ""
                    addDialogQuantity = "1"
                    addDialogLocation = ItemLocation.PANTRY
                },
                containerColor = ShelfScanGreen,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        },
        bottomBar = {
            ShelfScanBottomBar(
                currentTab = "Inventory",
                onInventoryClick = { },
                onScannerClick = onScannerClick
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectTapGestures { focusManager.clearFocus() }
                }
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ShelfScanGreen)
                }
            } else if (viewMode == ViewMode.LIST) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    item {
                        InventoryHeaderContent(
                            contentPadding = 16.dp,
                            expiringSoon = expiringSoon,
                            expired = expired,
                            searchQuery = searchQuery,
                            onQueryChange = { searchQuery = it },
                            selectedCategory = selectedCategory,
                            onCategorySelect = { selectedCategory = it },
                            statusFilter = statusFilter,
                            onStatusFilterSelect = { statusFilter = it },
                            viewMode = viewMode,
                            onViewModeChange = { viewMode = it }
                        )
                    }
                    items(filteredItems) { item ->
                        InventoryListRow(
                            item = item,
                            onClick = { onItemClick(item.id) },
                            onDeleteClick = { item.firebaseId?.let { viewModel.deleteItem(it) } }
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    item(span = { GridItemSpan(2) }) {
                        InventoryHeaderContent(
                            contentPadding = 16.dp,
                            expiringSoon = expiringSoon,
                            expired = expired,
                            searchQuery = searchQuery,
                            onQueryChange = { searchQuery = it },
                            selectedCategory = selectedCategory,
                            onCategorySelect = { selectedCategory = it },
                            statusFilter = statusFilter,
                            onStatusFilterSelect = { statusFilter = it },
                            viewMode = viewMode,
                            onViewModeChange = { viewMode = it }
                        )
                    }
                    items(filteredItems) { item ->
                        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
                            InventoryGridCard(
                                item = item,
                                onClick = { onItemClick(item.id) },
                                onDeleteClick = { item.firebaseId?.let { viewModel.deleteItem(it) } }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.writeInProgress) {
        SyncLoadingOverlay()
    }
    if (showAddDialog) {
        AddDetailsDialog(
            isManualEntry = true,
            name = addDialogName,
            expiry = addDialogExpiry,
            quantity = addDialogQuantity,
            location = addDialogLocation,
            onNameChange = { addDialogName = it },
            onExpiryChange = { addDialogExpiry = it },
            onQuantityChange = { addDialogQuantity = it.filter { c -> c.isDigit() || c == '.' } },
            onLocationChange = { addDialogLocation = it },
            onConfirm = {
                val name = addDialogName.trim().ifBlank { "Manual item" }
                val expiry = addDialogExpiry.trim().takeIf { it.isNotBlank() }
                val qty = addDialogQuantity.toFloatOrNull() ?: 1f
                viewModel.upsertItem(
                    PantryModel(
                        foodName = name,
                        dateScanned = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                        expiryDate = expiry,
                        quantity = qty,
                        location = addDialogLocation.name
                    )
                )
                showAddDialog = false
                addDialogName = ""
                addDialogExpiry = ""
                addDialogQuantity = "1"
                addDialogLocation = ItemLocation.PANTRY
            },
            onDismiss = {
                showAddDialog = false
                addDialogName = ""
                addDialogExpiry = ""
                addDialogQuantity = "1"
                addDialogLocation = ItemLocation.PANTRY
            }
        )
    }
    }
}

@Composable
private fun InventoryHeaderContent(
    contentPadding: androidx.compose.ui.unit.Dp,
    expiringSoon: Int,
    expired: Int,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    statusFilter: String,
    onStatusFilterSelect: (String) -> Unit,
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = contentPadding)
        ) {
            SummaryCards(expiringSoon = expiringSoon, expired = expired)
            Spacer(modifier = Modifier.height(16.dp))
            SearchBar(
                query = searchQuery,
                onQueryChange = onQueryChange,
                placeholder = "Search ingredients..."
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterDropdown(
                    modifier = Modifier.weight(1f),
                    label = "Location",
                    selected = selectedCategory,
                    options = listOf("All", "Fridge", "Freezer", "Pantry", "Other"),
                    onSelect = onCategorySelect
                )
                FilterDropdown(
                    modifier = Modifier.weight(1f),
                    label = "Status",
                    selected = statusFilter,
                    options = listOf("All") + ExpiryStatus.entries.map { it.label },
                    onSelect = onStatusFilterSelect
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "My Inventory",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                MyInventoryHeader(
                    viewMode = viewMode,
                    onViewModeChange = onViewModeChange
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ShelfScanTopBar(itemCount: Int = 0) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.statusBarsPadding(),
        color = colorScheme.surface,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "ShelfScanApp",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    "Tracking $itemCount items",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SummaryCards(expiringSoon: Int, expired: Int) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            title = "EXPIRING SOON",
            count = expiringSoon,
            subtitle = "Next 48 hours",
            accentColor = ExpiringSoonOrange,
            containerColor = colorScheme.surfaceVariant
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            title = "EXPIRED",
            count = expired,
            subtitle = "Past the date",
            accentColor = ExpiredRed,
            containerColor = colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    count: Int,
    subtitle: String,
    accentColor: Color,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(52.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "$count",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String
) {
    val colorScheme = MaterialTheme.colorScheme
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)) },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = colorScheme.onSurfaceVariant
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = colorScheme.surfaceVariant,
            unfocusedContainerColor = colorScheme.surfaceVariant,
            focusedTextColor = colorScheme.onSurfaceVariant,
            unfocusedTextColor = colorScheme.onSurfaceVariant,
            cursorColor = ShelfScanGreen,
            focusedLeadingIconColor = colorScheme.onSurfaceVariant,
            unfocusedLeadingIconColor = colorScheme.onSurfaceVariant
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(
    modifier: Modifier = Modifier,
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 12.sp, color = colorScheme.onSurface) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colorScheme.outline,
                unfocusedBorderColor = colorScheme.outline,
                focusedTextColor = colorScheme.onSurface,
                unfocusedTextColor = colorScheme.onSurface,
                focusedContainerColor = colorScheme.surface,
                unfocusedContainerColor = colorScheme.surface,
                focusedLabelColor = colorScheme.onSurface,
                unfocusedLabelColor = colorScheme.onSurface,
                cursorColor = colorScheme.primary,
                focusedTrailingIconColor = colorScheme.onSurface,
                unfocusedTrailingIconColor = colorScheme.onSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = colorScheme.surface
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option,
                            color = if (option == selected) colorScheme.primary else colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun MyInventoryHeader(
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onViewModeChange(ViewMode.LIST) }) {
                Icon(
                    Icons.Default.ViewList,
                    contentDescription = "List view",
                    tint = if (viewMode == ViewMode.LIST) ShelfScanGreen else colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onViewModeChange(ViewMode.GRID) }) {
                Icon(
                    Icons.Default.GridView,
                    contentDescription = "Grid view",
                    tint = if (viewMode == ViewMode.GRID) ShelfScanGreen else colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InventoryListRow(
    item: InventoryItem,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    val (avatarBg, avatarText) = avatarColorsFor(item.name)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(avatarBg)
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(avatarBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    item.name.first().uppercase(),
                    color = avatarText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = item.location.chipBg
                    ) {
                        Text(
                            item.location.label,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = item.location.chipText
                        )
                    }
                    Text(
                        item.expiryDisplay,
                        fontSize = 12.sp,
                        color = item.expiryStatus.colour
                    )
                }
                Text(
                    item.quantity,
                    fontSize = 12.sp,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = "Options",
                        tint = colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    DropdownMenuItem(
                        text = { Text("Delete", color = ExpiredRed) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = ExpiredRed
                            )
                        },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }
            }
        }
    }
}

@Composable
private fun InventoryGridCard(
    item: InventoryItem,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    val (avatarBg, avatarText) = avatarColorsFor(item.name)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(avatarBg)
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(avatarBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            item.name.first().uppercase(),
                            color = avatarText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Outlined.MoreVert,
                                contentDescription = "Options",
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete", color = ExpiredRed) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = ExpiredRed
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    item.name,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = item.location.chipBg
                    ) {
                        Text(
                            item.location.label,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = item.location.chipText
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = item.expiryStatus.colour.copy(alpha = 0.15f)
                ) {
                    Text(
                        item.expiryDisplay,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = item.expiryStatus.colour,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    item.quantity,
                    fontSize = 11.sp,
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun ShelfScanBottomBar(
    currentTab: String,
    onInventoryClick: () -> Unit,
    onScannerClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    NavigationBar(
        containerColor = colorScheme.surface,
        contentColor = colorScheme.onSurface,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(24.dp)) },
            label = { Text("Inventory", color = colorScheme.onSurface) },
            selected = currentTab == "Inventory",
            onClick = onInventoryClick
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(24.dp)) },
            label = { Text("Scanner", color = colorScheme.onSurface) },
            selected = currentTab == "Scanner",
            onClick = onScannerClick
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryListScreenPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme {
        InventoryListScreen(
            onItemClick = {},
            onScannerClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ShelfScanTopBarPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme { ShelfScanTopBar() }
}

@Preview(showBackground = true)
@Composable
private fun SummaryCardsPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme {
        SummaryCards(expiringSoon = 5, expired = 2)
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryCardPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme {
        SummaryCard(
            title = "EXPIRING SOON",
            count = 5,
            subtitle = "Next 48 hours",
            accentColor = ExpiringSoonOrange
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchBarPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme {
        SearchBar(query = "", onQueryChange = {}, placeholder = "Search ingredients...")
    }
}


@Preview(showBackground = true)
@Composable
private fun MyInventoryHeaderPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme {
        MyInventoryHeader(viewMode = ViewMode.LIST, onViewModeChange = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ShelfScanBottomBarPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme {
        ShelfScanBottomBar(
            currentTab = "Inventory",
            onInventoryClick = {},
            onScannerClick = {}
        )
    }
}
