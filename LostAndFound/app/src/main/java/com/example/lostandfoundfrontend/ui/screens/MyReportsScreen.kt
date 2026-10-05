package com.example.lostandfoundfrontend.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lostandfoundfrontend.data.ItemsUiState
import com.example.lostandfoundfrontend.data.LostFoundViewModel
import com.example.lostandfoundfrontend.model.Item
import com.example.lostandfoundfrontend.model.ItemStatus
import com.example.lostandfoundfrontend.ui.components.ShimmerItemCard
import com.example.lostandfoundfrontend.ui.components.animatedCount
import com.example.lostandfoundfrontend.ui.components.bounceClick
import com.example.lostandfoundfrontend.ui.components.staggeredEntrance
import com.example.lostandfoundfrontend.ui.theme.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MyReportsScreen(
    viewModel: LostFoundViewModel,
    onBack: () -> Unit,
    onItemClick: (Item) -> Unit
) {
    val myItemsState by viewModel.myItemsState.collectAsState()
    val snackbarHost = rememberToastHost(viewModel)
    var pendingDelete by remember { mutableStateOf<Item?>(null) }

    LaunchedEffect(Unit) { viewModel.loadMyItems() }

    pendingDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = LostRed) },
            title = { Text("Delete report?", fontWeight = FontWeight.Bold) },
            text = { Text("“${item.title}” will be removed for everyone. This can't be undone.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteItem(item.id); pendingDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = LostRed),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel", color = TextPrimary) } },
            shape = RoundedCornerShape(18.dp),
            containerColor = PaperWhite
        )
    }

    ItemListScaffold(
        title = "My Reports",
        state = myItemsState,
        onBack = onBack,
        onRetry = { viewModel.loadMyItems() },
        snackbarHost = snackbarHost,
        emptyEmoji = "📋",
        emptyTitle = "No reports yet",
        emptySubtitle = "Items you report will appear here.",
        header = { items ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryCard(animatedCount(items.size.toLong()), "Total", Modifier.weight(1f).staggeredEntrance(0))
                SummaryCard(animatedCount(items.count { !it.isResolved }.toLong()), "Open", Modifier.weight(1f).staggeredEntrance(1))
                SummaryCard(animatedCount(items.count { it.isResolved }.toLong()), "Resolved", Modifier.weight(1f).staggeredEntrance(2))
            }
        }
    ) { item, modifier ->
        MyReportCard(item = item, modifier = modifier, onClick = { onItemClick(item) }, onDelete = { pendingDelete = item })
    }
}

@Composable
fun SavedItemsScreen(
    viewModel: LostFoundViewModel,
    onBack: () -> Unit,
    onItemClick: (Item) -> Unit
) {
    val savedState by viewModel.savedItemsState.collectAsState()
    val snackbarHost = rememberToastHost(viewModel)

    LaunchedEffect(Unit) { viewModel.loadSavedItems() }

    ItemListScaffold(
        title = "Saved Items",
        state = savedState,
        onBack = onBack,
        onRetry = { viewModel.loadSavedItems() },
        snackbarHost = snackbarHost,
        emptyEmoji = "🔖",
        emptyTitle = "Nothing saved yet",
        emptySubtitle = "Tap the bookmark on any item to keep an eye on it."
    ) { item, modifier ->
        ItemCard(item = item, onClick = { onItemClick(item) }, modifier = modifier)
    }
}

/** Shared list chrome: top bar, skeletons, error/empty states, animated item placement. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ItemListScaffold(
    title: String,
    state: ItemsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    snackbarHost: SnackbarHostState,
    emptyEmoji: String,
    emptyTitle: String,
    emptySubtitle: String,
    header: (@Composable (List<Item>) -> Unit)? = null,
    itemContent: @Composable (Item, Modifier) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PaperWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PaperWhite)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Charcoal)
            )
        },
        snackbarHost = { AppSnackbarHost(snackbarHost) },
        containerColor = IvoryWhite
    ) { padding ->
        when {
            state.isLoading -> {
                Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(4) { ShimmerItemCard(Modifier.staggeredEntrance(it)) }
                }
            }
            state.error != null && state.items.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Text(state.error, color = LostRed, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Charcoal)) { Text("Retry") }
                    }
                }
            }
            state.items.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.staggeredEntrance(0)) {
                        Text(emptyEmoji, fontSize = 52.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(emptyTitle, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(emptySubtitle, fontSize = 13.sp, color = TextSecond, textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 40.dp))
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (header != null) {
                        item(key = "header") {
                            header(state.items)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                    itemsIndexed(state.items, key = { _, item -> item.id }) { index, item ->
                        // animateItemPlacement slides the rest of the list up when a card is deleted
                        itemContent(item, Modifier.animateItemPlacement().staggeredEntrance(index + 1))
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(value: String, label: String, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = PaperWhite, shadowElevation = 2.dp) {
        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Charcoal)
            Text(label, fontSize = 11.sp, color = TextSecond)
        }
    }
}

@Composable
private fun MyReportCard(item: Item, modifier: Modifier, onClick: () -> Unit, onDelete: () -> Unit) {
    val isLost = item.status == ItemStatus.LOST
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = PaperWhite,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth().bounceClick(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            when {
                                item.isResolved -> listOf(TextHint, TextHint.copy(alpha = 0.3f))
                                isLost -> listOf(LostRed, LostRed.copy(alpha = 0.3f))
                                else -> listOf(FoundGreen, FoundGreen.copy(alpha = 0.3f))
                            }
                        )
                    )
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.imageUrl != null) {
                        AsyncImage(model = item.imageUrl, contentDescription = item.title,
                            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Text(categoryEmoji(item), fontSize = 24.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(item.location.ifBlank { "—" }, fontSize = 12.sp, color = TextSecond, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(item.reportedAt, fontSize = 11.sp, color = TextHint)
                    if (item.claimedByName != null) {
                        Text("Claimed by ${item.claimedByName}", fontSize = 11.sp, color = FoundGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    StatusBadge(item.status, item.isResolved)
                    Spacer(modifier = Modifier.height(6.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LostRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
