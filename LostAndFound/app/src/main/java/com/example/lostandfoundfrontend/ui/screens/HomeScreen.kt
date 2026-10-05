package com.example.lostandfoundfrontend.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lostandfoundfrontend.data.LostFoundViewModel
import com.example.lostandfoundfrontend.model.Item
import com.example.lostandfoundfrontend.model.ItemStatus
import com.example.lostandfoundfrontend.ui.components.ServerWakingBanner
import com.example.lostandfoundfrontend.ui.components.ShimmerItemCard
import com.example.lostandfoundfrontend.ui.components.animatedCount
import com.example.lostandfoundfrontend.ui.components.bounceClick
import com.example.lostandfoundfrontend.ui.components.staggeredEntrance
import com.example.lostandfoundfrontend.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: LostFoundViewModel,
    onItemClick: (Item) -> Unit,
    onReportClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val itemsState by viewModel.itemsState.collectAsState()
    val statsState by viewModel.statsState.collectAsState()
    val serverWaking by viewModel.serverWaking.collectAsState()
    val snackbarHost = rememberToastHost(viewModel)

    // Saveable so the filter survives going to a detail screen and back
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedTab by rememberSaveable { mutableStateOf<ItemStatus?>(null) }

    LaunchedEffect(Unit) { viewModel.loadStats() }

    // Debounced reload whenever the filter/search changes (also the initial load)
    LaunchedEffect(selectedTab, searchQuery) {
        if (searchQuery.isNotEmpty()) delay(350)
        viewModel.loadItems(status = selectedTab?.name, search = searchQuery.trim().ifBlank { null })
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
        snackbarHost = { AppSnackbarHost(snackbarHost) },
        containerColor = IvoryWhite
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            item(key = "header") {
                TopHeaderBar(subtitle = "${animatedCount(statsState.total)} items reported")
                ServerWakingBanner(serverWaking)
            }

            // Hero banner with live stats
            item(key = "hero") {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(Charcoal, Slate, SlateLight)))
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    Column {
                        Text("Find what you lost,", fontSize = 13.sp, color = PaperWhite.copy(alpha = 0.65f),
                            modifier = Modifier.staggeredEntrance(0))
                        Text("Return what you found.", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = PaperWhite,
                            modifier = Modifier.staggeredEntrance(1))
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = Modifier.staggeredEntrance(2)) {
                            BannerStat(animatedCount(statsState.lost), "Lost")
                            Box(modifier = Modifier.width(1.dp).height(32.dp).background(PaperWhite.copy(alpha = 0.2f)))
                            BannerStat(animatedCount(statsState.found), "Found")
                            Box(modifier = Modifier.width(1.dp).height(32.dp).background(PaperWhite.copy(alpha = 0.2f)))
                            BannerStat(animatedCount(statsState.resolved), "Reunited")
                        }
                    }
                }
            }

            // Search bar
            item(key = "search") {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search items, locations, categories...", color = TextHint, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecond, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        AnimatedVisibility(visible = searchQuery.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = TextSecond, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
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
            item(key = "filters") {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { FilterPill("All", selectedTab == null) { selectedTab = null } }
                    item { FilterPill("Lost", selectedTab == ItemStatus.LOST) { selectedTab = ItemStatus.LOST } }
                    item { FilterPill("Found", selectedTab == ItemStatus.FOUND) { selectedTab = ItemStatus.FOUND } }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Count header + refresh indicator
            item(key = "count") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${itemsState.items.size} items", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text("Recent first", fontSize = 12.sp, color = TextSecond)
                }
                Box(Modifier.fillMaxWidth().height(10.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.animation.AnimatedVisibility(itemsState.isRefreshing, enter = fadeIn(), exit = fadeOut()) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(2.dp).clip(RoundedCornerShape(2.dp)),
                            color = Charcoal, trackColor = StrokeGray
                        )
                    }
                }
            }

            when {
                itemsState.isLoading -> {
                    items(4) { i ->
                        ShimmerItemCard(Modifier.padding(horizontal = 16.dp, vertical = 5.dp).staggeredEntrance(i))
                    }
                }
                itemsState.error != null && itemsState.items.isEmpty() -> {
                    item(key = "error") {
                        ErrorState(itemsState.error ?: "Failed to load") { viewModel.loadItems() }
                    }
                }
                itemsState.items.isEmpty() -> {
                    item(key = "empty") { EmptyState(searchQuery, onReportClick) }
                }
                else -> {
                    itemsIndexed(itemsState.items, key = { _, item -> item.id }) { index, item ->
                        ItemCard(
                            item = item, onClick = { onItemClick(item) },
                            modifier = Modifier
                                .animateItemPlacement()
                                .padding(horizontal = 16.dp, vertical = 5.dp)
                                .staggeredEntrance(index)
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
    val bg by animateColorAsState(if (selected) Charcoal else PaperWhite, tween(250), label = "pillBg")
    val fg by animateColorAsState(if (selected) PaperWhite else TextSecond, tween(250), label = "pillFg")
    Box(
        modifier = Modifier.clip(RoundedCornerShape(20.dp))
            .background(bg)
            .bounceClick(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 9.dp)
    ) {
        Text(label, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, color = fg)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp).staggeredEntrance(0),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📡", fontSize = 44.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(message, color = LostRed, textAlign = TextAlign.Center, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Charcoal),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Retry", color = PaperWhite)
        }
    }
}

@Composable
private fun EmptyState(searchQuery: String, onReportClick: () -> Unit) {
    // Gentle floating emoji
    val float = rememberInfiniteTransition(label = "float")
    val dy by float.animateFloat(0f, -10f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "dy")
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp).staggeredEntrance(0),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Crossfade(targetState = searchQuery.isNotBlank(), label = "emptyEmoji") { searching ->
            Text(if (searching) "🔍" else "📭", fontSize = 52.sp, modifier = Modifier.graphicsLayer { translationY = dy })
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            if (searchQuery.isNotBlank()) "No results for \"$searchQuery\"" else "No items yet",
            fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPrimary, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text("Be the first to report a lost or found item.", fontSize = 13.sp, color = TextSecond, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onReportClick, shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Charcoal)) {
            Text("Report an item", color = Charcoal, fontWeight = FontWeight.SemiBold)
        }
    }
}
