package com.example.lostandfoundfrontend.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lostandfoundfrontend.model.Item
import com.example.lostandfoundfrontend.model.ItemStatus
import com.example.lostandfoundfrontend.ui.theme.*

internal fun categoryEmoji(title: String): String {
    val t = title.lowercase()
    return when {
        t.contains("laptop") || t.contains("macbook") || t.contains("computer") -> "💻"
        t.contains("phone") || t.contains("mobile") -> "📱"
        t.contains("key") -> "🔑"
        t.contains("bag") || t.contains("backpack") -> "🎒"
        t.contains("wallet") || t.contains("purse") -> "👛"
        t.contains("book") || t.contains("notebook") -> "📚"
        t.contains("card") || t.contains("id") -> "🪪"
        t.contains("glasses") || t.contains("spectacles") -> "👓"
        t.contains("watch") -> "⌚"
        t.contains("earphone") || t.contains("airpod") || t.contains("headphone") -> "🎧"
        t.contains("bottle") -> "🍶"
        t.contains("umbrella") -> "☂️"
        else -> "📦"
    }
}

@Composable
fun ItemCard(item: Item, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.6f), label = "scale"
    )
    val isLost = item.status == ItemStatus.LOST

    Surface(
        modifier = modifier.fillMaxWidth().scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                    onTap = { onClick() }
                )
            },
        shape = RoundedCornerShape(16.dp),
        color = PaperWhite,
        shadowElevation = 3.dp,
        tonalElevation = 0.dp
    ) {
        Column {
            // Top accent bar
            Box(
                modifier = Modifier.fillMaxWidth().height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            if (isLost) listOf(LostRed, LostRed.copy(alpha = 0.4f))
                            else listOf(FoundGreen, FoundGreen.copy(alpha = 0.4f))
                        )
                    )
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Emoji icon box
                    Box(
                        modifier = Modifier.size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(categoryEmoji(item.title), fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                item.title, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                                color = TextPrimary, modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Status pill
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isLost) LostRedBg else FoundGreenBg)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    item.status.name, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                                    color = if (isLost) LostRed else FoundGreen
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextSecond, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(item.location, fontSize = 12.sp, color = TextSecond)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = TextHint, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(item.reportedAt, fontSize = 11.sp, color = TextHint)
                        }
                    }
                }

                if (item.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(item.description, fontSize = 12.sp, color = TextSecond, maxLines = 2, lineHeight = 18.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = StrokeGray, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(22.dp).clip(RoundedCornerShape(6.dp))
                                .background(Charcoal),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(item.reporterName.take(1), color = PaperWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(item.reporterName, fontSize = 11.sp, color = TextSecond)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("View Details", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Charcoal)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Charcoal, modifier = Modifier.size(13.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: ItemStatus) {
    val isLost = status == ItemStatus.LOST
    Box(
        modifier = Modifier.clip(RoundedCornerShape(20.dp))
            .background(if (isLost) LostRedBg else FoundGreenBg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(status.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isLost) LostRed else FoundGreen)
    }
}
