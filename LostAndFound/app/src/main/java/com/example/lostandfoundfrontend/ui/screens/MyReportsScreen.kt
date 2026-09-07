package com.example.lostandfoundfrontend.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportsScreen(
    viewModel: LostFoundViewModel,
    onBack: () -> Unit,
    onItemClick: (Item) -> Unit
) {
    val myItemsState by viewModel.myItemsState.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMyItems() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Reports", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PaperWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PaperWhite)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Charcoal)
            )
        },
        containerColor = IvoryWhite
    ) { padding ->
        when {
            myItemsState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Charcoal)
                }
            }
            myItemsState.error != null -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(myItemsState.error ?: "Error loading items", color = LostRed, textAlign = TextAlign.Center)
                }
            }
            myItemsState.items.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 52.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No reports yet", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Items you report will appear here.", fontSize = 13.sp, color = TextSecond, textAlign = TextAlign.Center)
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SummaryCard("${myItemsState.items.size}", "Total", Modifier.weight(1f))
                            SummaryCard("${myItemsState.items.count { it.status == ItemStatus.LOST }}", "Lost", Modifier.weight(1f))
                            SummaryCard("${myItemsState.items.count { it.status == ItemStatus.FOUND }}", "Found", Modifier.weight(1f))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    itemsIndexed(myItemsState.items) { index, item ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { kotlinx.coroutines.delay(index * 60L); visible = true }
                        AnimatedVisibility(visible = visible, enter = slideInVertically { it / 3 } + fadeIn()) {
                            MyReportCard(item = item, onClick = { onItemClick(item) }, onDelete = { viewModel.deleteItem(item.id) })
                        }
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
private fun MyReportCard(item: Item, onClick: () -> Unit, onDelete: () -> Unit) {
    val isLost = item.status == ItemStatus.LOST
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = PaperWhite,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            if (isLost) listOf(LostRed, LostRed.copy(alpha = 0.3f))
                            else listOf(FoundGreen, FoundGreen.copy(alpha = 0.3f))
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
                ) { Text(categoryEmoji(item.title), fontSize = 24.sp) }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Text(item.location, fontSize = 12.sp, color = TextSecond)
                    Text(item.reportedAt, fontSize = 11.sp, color = TextHint)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(20.dp))
                            .background(if (isLost) LostRedBg else FoundGreenBg)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(item.status.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isLost) LostRed else FoundGreen)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = LostRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
