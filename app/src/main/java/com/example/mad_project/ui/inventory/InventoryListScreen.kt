package com.example.mad_project.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mad_project.data.InventoryItem
import com.example.mad_project.data.ItemLocation
import com.example.mad_project.data.sampleInventoryItems
import com.example.mad_project.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryListScreen(
    onAddClick: () -> Unit,
    onItemClick: (Long) -> Unit,
    onShoppingClick: () -> Unit,
    onRecipesClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAlertsClick: () -> Unit = {},
    alertCount: Int = 0,
    expiringSoonCount: Int = 0,
    expiredCount: Int = 0,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var statusFilter by remember { mutableStateOf("All") }
    val items = remember { sampleInventoryItems() }
    val filteredItems = items
        .filter { it.name.contains(searchQuery, ignoreCase = true) }
        .filter { selectedCategory == "All" || it.location.name == selectedCategory.uppercase() }

    Scaffold(
        topBar = { ShelfScanTopBar(onAlertsClick = onAlertsClick, alertCount = alertCount) },
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
                onListClick = onShoppingClick,
                onRecipesClick = onRecipesClick,
                onSettingsClick = onSettingsClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            SummaryCards(expiringSoon = expiringSoonCount, expired = expiredCount)
            Spacer(modifier = Modifier.height(16.dp))
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search ingredients..."
            )
            Spacer(modifier = Modifier.height(12.dp))
            CategoryChips(
                selected = selectedCategory,
                onSelect = { selectedCategory = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
            MyInventoryHeader(statusFilter = statusFilter, onStatusChange = { statusFilter = it })
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredItems) { item ->
                    InventoryListRow(
                        item = item,
                        onClick = { onItemClick(item.id) },
                        onMenuClick = { }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShelfScanTopBar(onAlertsClick: () -> Unit, alertCount: Int) {
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
                    "Tracking ${sampleInventoryItems().size} items",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            BadgedBox(badge = { if (alertCount > 0) Badge { Text("$alertCount") } }) {
                IconButton(onClick = onAlertsClick) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = TextSecondary
                    )
                }
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

@Composable
private fun CategoryChips(selected: String, onSelect: (String) -> Unit) {
    val categories = listOf("All", "Fridge", "Pantry", "Freezer")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { cat ->
            FilterChip(
                selected = selected == cat,
                onClick = { onSelect(cat) },
                label = { Text(cat) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ShelfScanGreen,
                    selectedLabelColor = Color.White,
                    containerColor = ChipBg,
                    labelColor = TextPrimary
                )
            )
        }
    }
}

@Composable
private fun MyInventoryHeader(statusFilter: String, onStatusChange: (String) -> Unit) {
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
        FilterChip(
            selected = false,
            onClick = { },
            label = { Text("Status: $statusFilter") },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = ShelfScanGreen,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = SurfaceVariant,
                labelColor = TextPrimary
            )
        )
    }
}

@Composable
private fun InventoryListRow(
    item: InventoryItem,
    onClick: () -> Unit,
    onMenuClick: () -> Unit
) {
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
            IconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Outlined.MoreVert,
                    contentDescription = "Options",
                    tint = TextSecondary
                )
            }
        }
    }
}

@Composable
fun ShelfScanBottomBar(
    currentTab: String,
    onInventoryClick: () -> Unit,
    onListClick: () -> Unit,
    onRecipesClick: () -> Unit,
    onSettingsClick: () -> Unit
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
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(24.dp)) },
            label = { Text("List") },
            selected = currentTab == "List",
            onClick = onListClick
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Restaurant, contentDescription = null, modifier = Modifier.size(24.dp)) },
            label = { Text("Recipes") },
            selected = currentTab == "Recipes",
            onClick = onRecipesClick
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(24.dp)) },
            label = { Text("Settings") },
            selected = currentTab == "Settings",
            onClick = onSettingsClick
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
            onShoppingClick = {},
            onRecipesClick = {},
            onSettingsClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ShelfScanTopBarPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme(dynamicColour = false) { ShelfScanTopBar(onAlertsClick = {}, alertCount = 0) }
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
private fun CategoryChipsPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme(dynamicColour = false) {
        CategoryChips(selected = "All", onSelect = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun MyInventoryHeaderPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme(dynamicColour = false) {
        MyInventoryHeader(statusFilter = "All", onStatusChange = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun InventoryListRowPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme(dynamicColour = false) {
        InventoryListRow(
            item = sampleInventoryItems().first(),
            onClick = {},
            onMenuClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ShelfScanBottomBarPreview() {
    com.example.mad_project.ui.theme.MADProjectTheme(dynamicColour = false) {
        ShelfScanBottomBar(
            currentTab = "Inventory",
            onInventoryClick = {},
            onListClick = {},
            onRecipesClick = {},
            onSettingsClick = {}
        )
    }
}
