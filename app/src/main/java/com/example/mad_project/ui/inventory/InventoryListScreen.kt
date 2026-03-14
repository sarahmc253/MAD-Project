package com.example.mad_project.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.mad_project.data.ItemLocation
import com.example.mad_project.ui.theme.*
import com.example.mad_project.ui.viewmodel.InventoryViewModel

private enum class ViewMode { LIST, GRID }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryListScreen(
    onAddClick: () -> Unit,
    onItemClick: (String) -> Unit,
    onScannerClick: () -> Unit,
    viewModel: InventoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var statusFilter by remember { mutableStateOf("All") }
    var viewMode by remember { mutableStateOf(ViewMode.LIST) }
    val filteredItems = uiState.items
        .filter { it.name.contains(searchQuery, ignoreCase = true) }
        .filter { selectedCategory == "All" || it.location.name == selectedCategory.uppercase() }
        .filter { statusFilter == "All" || it.expiryStatus.label == statusFilter }

    val expiringSoon = uiState.items.count { it.expiryStatus == ExpiryStatus.EXPIRES_SOON }
    val expired = uiState.items.count { it.expiryStatus == ExpiryStatus.EXPIRED }

    Scaffold(
        topBar = { ShelfScanTopBar(itemCount = uiState.items.size) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            SummaryCards(expiringSoon = expiringSoon, expired = expired)
            Spacer(modifier = Modifier.height(16.dp))
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search ingredients..."
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterDropdown(
                    modifier = Modifier.weight(1f),
                    label = "Location",
                    selected = selectedCategory,
                    options = listOf("All", "Fridge", "Pantry", "Freezer"),
                    onSelect = { selectedCategory = it }
                )
                FilterDropdown(
                    modifier = Modifier.weight(1f),
                    label = "Status",
                    selected = statusFilter,
                    options = listOf("All") + ExpiryStatus.entries.map { it.label },
                    onSelect = { statusFilter = it }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            MyInventoryHeader(
                viewMode = viewMode,
                onViewModeChange = { viewMode = it }
            )
            Spacer(modifier = Modifier.height(8.dp))
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
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp)
                ) {
                    items(filteredItems) { item ->
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

@Composable
private fun ShelfScanTopBar(itemCount: Int = 0) {
    Surface(
        modifier = Modifier.statusBarsPadding(),
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ShelfScanGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add, // placeholder for fridge icon
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
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
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { }) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SummaryCards(expiringSoon: Int, expired: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            modifier = Modifier.weight(1f),
            title = "EXPIRING SOON",
            count = expiringSoon,
            subtitle = "Next 48 hours",
            accentColor = ExpiringSoonOrange
        )
        SummaryCard(
            modifier = Modifier.weight(1f),
            title = "EXPIRED",
            count = expired,
            subtitle = "Past the date",
            accentColor = ExpiredRed
        )
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    count: Int,
    subtitle: String,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = accentColor
                )
                Text(
                    "$count",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary
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
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        placeholder = { Text(placeholder, color = TextSecondary) },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = TextSecondary
            )
        },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = SurfaceVariant,
            unfocusedContainerColor = SurfaceVariant,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = ShelfScanGreen,
            focusedLeadingIconColor = TextSecondary,
            unfocusedLeadingIconColor = TextSecondary
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
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 12.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ShelfScanGreen,
                unfocusedBorderColor = Color.LightGray,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = if (option == selected) ShelfScanGreen else TextPrimary) },
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "My Inventory",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onViewModeChange(ViewMode.LIST) }) {
                Icon(
                    Icons.Default.ViewList,
                    contentDescription = "List view",
                    tint = if (viewMode == ViewMode.LIST) ShelfScanGreen else TextSecondary
                )
            }
            IconButton(onClick = { onViewModeChange(ViewMode.GRID) }) {
                Icon(
                    Icons.Default.GridView,
                    contentDescription = "Grid view",
                    tint = if (viewMode == ViewMode.GRID) ShelfScanGreen else TextSecondary
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(item.location.chipBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    item.name.first().uppercase(),
                    color = item.location.chipText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
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
                    color = TextSecondary
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Outlined.MoreVert,
                        contentDescription = "Options",
                        tint = TextSecondary
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
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

@Composable
private fun InventoryGridCard(
    item: InventoryItem,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(item.location.chipBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        item.name.first().uppercase(),
                        color = item.location.chipText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
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
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                item.name,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
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
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                item.expiryDisplay,
                fontSize = 11.sp,
                color = item.expiryStatus.colour,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                item.quantity,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun ShelfScanBottomBar(
    currentTab: String,
    onInventoryClick: () -> Unit,
    onScannerClick: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(24.dp)) },
            label = { Text("Inventory") },
            selected = currentTab == "Inventory",
            onClick = onInventoryClick
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(24.dp)) },
            label = { Text("Scanner") },
            selected = currentTab == "Scanner",
            onClick = onScannerClick
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryListScreenPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme(dynamicColour = false) {
        InventoryListScreen(
            onAddClick = {},
            onItemClick = {},
            onScannerClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ShelfScanTopBarPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme(dynamicColour = false) { ShelfScanTopBar() }
}

@Preview(showBackground = true)
@Composable
private fun SummaryCardsPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme(dynamicColour = false) {
        SummaryCards(expiringSoon = 5, expired = 2)
    }
}

@Preview(showBackground = true)
@Composable
private fun SummaryCardPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme(dynamicColour = false) {
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
    com.example.mad_project.ui.theme.MADProjectTheme(dynamicColour = false) {
        SearchBar(query = "", onQueryChange = {}, placeholder = "Search ingredients...")
    }
}


@Preview(showBackground = true)
@Composable
private fun MyInventoryHeaderPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme(dynamicColour = false) {
        MyInventoryHeader(viewMode = ViewMode.LIST, onViewModeChange = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ShelfScanBottomBarPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme(dynamicColour = false) {
        ShelfScanBottomBar(
            currentTab = "Inventory",
            onInventoryClick = {},
            onScannerClick = {}
        )
    }
}
