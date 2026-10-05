package com.example.lostandfoundfrontend.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.lostandfoundfrontend.data.LostFoundViewModel
import com.example.lostandfoundfrontend.data.TokenStore
import com.example.lostandfoundfrontend.model.Item
import com.example.lostandfoundfrontend.model.ItemStatus
import com.example.lostandfoundfrontend.ui.components.SuccessOverlay
import com.example.lostandfoundfrontend.ui.components.staggeredEntrance
import com.example.lostandfoundfrontend.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    item: Item,
    viewModel: LostFoundViewModel,
    onBack: () -> Unit
) {
    val isLost = item.status == ItemStatus.LOST
    val isMine = item.reporterId.isNotEmpty() && item.reporterId == TokenStore.getUserId()
    val context = LocalContext.current
    var showContact by remember { mutableStateOf(false) }
    var showClaimDialog by remember { mutableStateOf(false) }
    var claimMessage by remember { mutableStateOf("") }
    var showSuccess by remember { mutableStateOf(false) }

    val detailState by viewModel.detailState.collectAsState()
    val snackbarHost = rememberToastHost(viewModel)

    // Bookmark pops when toggled
    val scope = rememberCoroutineScope()
    val bookmarkScale = remember { Animatable(1f) }

    if (showClaimDialog) {
        AlertDialog(
            onDismissRequest = { if (!detailState.isClaiming) showClaimDialog = false },
            icon = { Icon(if (isMine) Icons.Default.TaskAlt else Icons.Default.Handshake, contentDescription = null, tint = Charcoal) },
            title = {
                Text(
                    when { isMine -> "Mark as Resolved?"; isLost -> "I Found This Item"; else -> "This is Mine" },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                if (isMine) {
                    Text("This closes the report so others know it has been ${if (isLost) "found" else "returned"}.", fontSize = 14.sp, color = TextSecond)
                } else {
                    Column {
                        Text(
                            if (isLost) "Tell ${item.reporterName} where you found it and how to get it back:"
                            else "Describe something only the owner would know, so ${item.reporterName} can verify it's yours:",
                            fontSize = 13.sp, color = TextSecond
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = claimMessage,
                            onValueChange = { claimMessage = it.take(500) },
                            placeholder = { Text("Your message…", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 90.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Charcoal, unfocusedBorderColor = StrokeGray,
                                focusedContainerColor = PaperWhite, unfocusedContainerColor = PaperWhite
                            )
                        )
                    }
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = if (isMine) "Mark Resolved" else "Submit",
                    loading = detailState.isClaiming,
                    enabled = isMine || claimMessage.isNotBlank(),
                    modifier = Modifier.width(150.dp)
                ) {
                    viewModel.claimItem(item.id, claimMessage) {
                        showClaimDialog = false
                        showSuccess = true
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showClaimDialog = false }, enabled = !detailState.isClaiming) {
                    Text("Cancel", color = TextPrimary)
                }
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = PaperWhite
        )
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Item Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PaperWhite) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PaperWhite)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            viewModel.toggleSave(item)
                            scope.launch {
                                bookmarkScale.snapTo(0.6f)
                                bookmarkScale.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMedium))
                            }
                        }) {
                            Crossfade(item.isSaved, label = "bookmark") { saved ->
                                Icon(
                                    if (saved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = if (saved) "Remove from saved" else "Save",
                                    tint = if (saved) AccentGold else PaperWhite,
                                    modifier = Modifier.graphicsLayer { scaleX = bookmarkScale.value; scaleY = bookmarkScale.value }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Charcoal)
                )
            },
            snackbarHost = { AppSnackbarHost(snackbarHost) },
            containerColor = IvoryWhite
        ) { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())
            ) {
                DetailHero(item)

                // Content card slides up over the hero
                Surface(
                    modifier = Modifier.fillMaxWidth().offset(y = (-16).dp).staggeredEntrance(1, offsetY = 60.dp),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = IvoryWhite
                ) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {

                        Text(item.title, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.reportedAt, fontSize = 12.sp, color = TextSecond)

                        // Resolved banner animates in when the item gets claimed
                        AnimatedVisibility(
                            visible = item.isResolved,
                            enter = expandVertically(spring(dampingRatio = 0.7f)) + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            ResolvedBanner(item, isMine)
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = StrokeGray)
                        Spacer(modifier = Modifier.height(16.dp))

                        DetailInfoRow(Icons.Default.LocationOn, if (isLost) "Last seen at" else "Found at",
                            item.location.ifBlank { "Not specified" }, Modifier.staggeredEntrance(2))
                        DetailInfoRow(Icons.Default.CalendarToday, "Date Reported",
                            item.reportedAt.ifBlank { "Unknown" }, Modifier.staggeredEntrance(3))
                        DetailInfoRow(Icons.Default.Category, "Category",
                            item.category.ifBlank { "General" }, Modifier.staggeredEntrance(4))

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = StrokeGray)
                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Description", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(shape = RoundedCornerShape(12.dp), color = PaperWhite, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                item.description.ifEmpty { "No description provided." },
                                fontSize = 14.sp, color = TextPrimary, lineHeight = 22.sp,
                                modifier = Modifier.padding(14.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = StrokeGray)
                        Spacer(modifier = Modifier.height(20.dp))

                        // Reporter card
                        Text("Reported By", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        ReporterCard(item, isMine)

                        Spacer(modifier = Modifier.height(24.dp))

                        AnimatedVisibility(visible = !item.isResolved, exit = shrinkVertically() + fadeOut()) {
                            Column {
                                PrimaryButton(
                                    text = when { isMine -> "Mark as Resolved"; isLost -> "I Found This Item"; else -> "This is Mine" },
                                    loading = false,
                                    icon = when { isMine -> Icons.Default.TaskAlt; isLost -> Icons.Default.Search; else -> Icons.Default.CheckCircle }
                                ) { showClaimDialog = true }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }

                        if (!isMine) {
                            OutlinedButton(
                                onClick = { showContact = !showContact },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Charcoal),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StrokeGray)
                            ) {
                                Icon(Icons.Default.ContactPhone, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (showContact) "Hide Contact" else "Contact Reporter", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            }

                            AnimatedVisibility(
                                visible = showContact,
                                enter = expandVertically(spring(dampingRatio = 0.8f)) + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                                    shape = RoundedCornerShape(12.dp), color = PaperWhite, shadowElevation = 1.dp
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        val contacts = buildList {
                                            if (item.contactInfo.isNotBlank()) add(item.contactInfo)
                                            if (item.reporterPhone.isNotBlank()) add(item.reporterPhone)
                                            if (item.reporterEmail.isNotBlank()) add(item.reporterEmail)
                                        }.distinct()
                                        if (contacts.isEmpty()) {
                                            Text("No contact details shared.", fontSize = 13.sp, color = TextSecond, modifier = Modifier.padding(8.dp))
                                        }
                                        contacts.forEach { contact -> ContactRow(contact) { openContact(context, contact) } }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }

        SuccessOverlay(
            visible = showSuccess,
            title = if (isMine) "Marked as resolved" else "Claim sent!",
            subtitle = if (isMine) "Great news — glad it worked out." else "${item.reporterName} can now see your message. Reach out using their contact details.",
            onFinished = { showSuccess = false }
        )
    }
}

@Composable
private fun DetailHero(item: Item) {
    val isLost = item.status == ItemStatus.LOST
    // Image/emoji zooms in when the screen opens
    val zoom = remember { Animatable(0.85f) }
    LaunchedEffect(Unit) { zoom.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow)) }
    Box(
        modifier = Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Charcoal, Slate)))
            .padding(top = 24.dp, bottom = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val hasImage = item.imageUrl != null
            Box(
                modifier = Modifier
                    .then(if (hasImage) Modifier.fillMaxWidth().height(220.dp).padding(horizontal = 20.dp) else Modifier.size(96.dp))
                    .graphicsLayer { scaleX = zoom.value; scaleY = zoom.value }
                    .clip(RoundedCornerShape(22.dp))
                    .background(PaperWhite.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (hasImage) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(item.imageUrl).crossfade(400).build(),
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(categoryEmoji(item), fontSize = 48.sp)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            StatusBadge(item.status, item.isResolved)
            if (!item.isResolved) {
                Spacer(Modifier.height(4.dp))
                Text(if (isLost) "Someone is looking for this" else "Waiting for its owner", fontSize = 12.sp, color = PaperWhite.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun ResolvedBanner(item: Item, isMine: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
        shape = RoundedCornerShape(12.dp),
        color = FoundGreenBg
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Verified, contentDescription = null, tint = FoundGreen, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (item.claimedByName != null) "Claimed by ${item.claimedByName}" else "This item has been resolved",
                    color = FoundGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp
                )
            }
            // The private claim message is only sent to the reporter and the claimer
            if (!item.claimMessage.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    (if (isMine) "Their message: " else "Your message: ") + "“${item.claimMessage}”",
                    fontSize = 13.sp, color = TextPrimary, lineHeight = 19.sp
                )
            }
        }
    }
}

@Composable
private fun ReporterCard(item: Item, isMine: Boolean) {
    Surface(
        shape = RoundedCornerShape(14.dp), color = PaperWhite,
        shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(50.dp)
                    .background(Brush.linearGradient(listOf(Charcoal, Slate)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    item.reporterName.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString(""),
                    color = PaperWhite, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(if (isMine) "${item.reporterName} (you)" else item.reporterName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                Text(item.reporterDept, fontSize = 12.sp, color = TextSecond)
            }
            Box(
                modifier = Modifier.clip(RoundedCornerShape(6.dp))
                    .background(FoundGreenBg).padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Student", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FoundGreen)
            }
        }
    }
}

@Composable
private fun ContactRow(value: String, onClick: () -> Unit) {
    val isEmail = value.contains("@")
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(if (isEmail) Icons.Default.Email else Icons.Default.Phone, contentDescription = null, tint = Charcoal, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(value, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
        Text(if (isEmail) "Email" else "Call", fontSize = 12.sp, color = Charcoal, fontWeight = FontWeight.SemiBold)
    }
}

private fun openContact(context: android.content.Context, value: String) {
    val digits = value.filter { it.isDigit() || it == '+' }
    val intent = when {
        value.contains("@") -> Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${value.trim()}"))
        digits.length >= 6 -> Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits"))
        else -> return
    }
    try { context.startActivity(intent) } catch (_: Exception) { }
}

@Composable
private fun DetailInfoRow(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(PaperWhite),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = TextSecond, modifier = Modifier.size(17.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextSecond)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}
