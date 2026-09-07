package com.example.lostandfoundfrontend.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lostandfoundfrontend.data.LostFoundViewModel
import com.example.lostandfoundfrontend.model.Item
import com.example.lostandfoundfrontend.model.ItemStatus
import com.example.lostandfoundfrontend.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: LostFoundViewModel,
    onItemClick: (Item) -> Unit,
    onReportClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val itemsState by viewModel.itemsState.collectAsState()
    val statsState by viewModel.statsState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf<ItemStatus?>(null) }

    // Load on first composition
    LaunchedEffect(Unit) {
        viewModel.loadItems()
        viewModel.loadStats()
    }

    // Re-fetch when filter/search changes (debounce via key)
    LaunchedEffect(selectedTab, searchQuery) {
        kotlinx.coroutines.delay(300)
        viewModel.loadItems(status = selectedTab?.name, search = searchQuery.ifBlank { null })
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedScreen = "Home",
                onHomeClick = {},
                onReportClick = onReportClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = IvoryWhite
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            item { TopHeaderBar(subtitle = "${statsState.total} items reported") }

            // Hero banner with live stats
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(Charcoal, Slate, SlateLight)))
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    Column {
                        Text("Find what you lost,", fontSize = 13.sp, color = PaperWhite.copy(alpha = 0.65f))
                        Text("Return what you found.", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = PaperWhite)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            BannerStat("${statsState.lost}", "Lost")
                            Box(modifier = Modifier.width(1.dp).height(32.dp).background(PaperWhite.copy(alpha = 0.2f)))
                            BannerStat("${statsState.found}", "Found")
                            Box(modifier = Modifier.width(1.dp).height(32.dp).background(PaperWhite.copy(alpha = 0.2f)))
                            BannerStat("${statsState.total}", "Total")
                        }
                    }
                }
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search items, locations...", color = TextHint, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecond, modifier = Modifier.size(20.dp)) },
                    trailingIcon = if (searchQuery.isNotBlank()) ({
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = TextSecond, modifier = Modifier.size(18.dp))
                        }
                    }) else null,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Charcoal, unfocusedBorderColor = StrokeGray,
                        focusedContainerColor = PaperWhite, unfocusedContainerColor = PaperWhite
                    ),
                    singleLine = true
                )
            }

            // Filter chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { FilterPill("All", selectedTab == null) { selectedTab = null } }
                    item { FilterPill("Lost", selectedTab == ItemStatus.LOST) { selectedTab = ItemStatus.LOST } }
                    item { FilterPill("Found", selectedTab == ItemStatus.FOUND) { selectedTab = ItemStatus.FOUND } }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Count header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${itemsState.items.size} items", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text("Recent first", fontSize = 12.sp, color = TextSecond)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            when {
                itemsState.isLoading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(56.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Charcoal)
                        }
                    }
                }
                itemsState.error != null -> {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(56.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("⚠️", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(itemsState.error ?: "Failed to load", color = LostRed, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { viewModel.loadItems() }, colors = ButtonDefaults.buttonColors(containerColor = Charcoal)) {
                                Text("Retry", color = PaperWhite)
                            }
                        }
                    }
                }
                itemsState.items.isEmpty() -> {
                    item { EmptyState(searchQuery) }
                }
                else -> {
                    itemsIndexed(itemsState.items) { _, item ->
                        ItemCard(
                            item = item, onClick = { onItemClick(item) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BannerStat(value: String, label: String) {
    Column {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = PaperWhite)
        Text(label, fontSize = 11.sp, color = PaperWhite.copy(alpha = 0.6f))
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(20.dp))
            .background(if (selected) Charcoal else PaperWhite)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 9.dp)
    ) {
        Text(
            label, fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) PaperWhite else TextSecond
        )
    }
}

@Composable
private fun EmptyState(searchQuery: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📭", fontSize = 52.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            if (searchQuery.isNotBlank()) "No results for \"$searchQuery\"" else "No items yet",
            fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text("Be the first to report a lost or found item.", fontSize = 13.sp, color = TextSecond, textAlign = TextAlign.Center)
    }
}
