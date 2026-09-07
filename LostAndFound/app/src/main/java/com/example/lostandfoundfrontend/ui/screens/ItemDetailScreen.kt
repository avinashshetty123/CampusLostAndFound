package com.example.lostandfoundfrontend.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lostandfoundfrontend.data.LostFoundViewModel
import com.example.lostandfoundfrontend.model.Item
import com.example.lostandfoundfrontend.model.ItemStatus
import com.example.lostandfoundfrontend.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    item: Item,
    viewModel: LostFoundViewModel,
    onBack: () -> Unit
) {
    val isLost = item.status == ItemStatus.LOST
    var showContact by remember { mutableStateOf(false) }
    var showClaimDialog by remember { mutableStateOf(false) }
    var claimMessage by remember { mutableStateOf("") }

    val detailState by viewModel.detailState.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            snackbarHostState.showSnackbar(toastMessage!!)
            viewModel.clearToast()
        }
    }

    if (showClaimDialog) {
        AlertDialog(
            onDismissRequest = { showClaimDialog = false },
            title = { Text(if (isLost) "I Found This Item" else "This is Mine", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Send a message to the reporter:", fontSize = 13.sp, color = TextSecond)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = claimMessage,
                        onValueChange = { claimMessage = it },
                        placeholder = { Text("Describe how you can verify this is yours...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Charcoal, unfocusedBorderColor = StrokeGray,
                            focusedContainerColor = PaperWhite, unfocusedContainerColor = PaperWhite
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.claimItem(item.id, claimMessage)
                        showClaimDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Charcoal),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Submit") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClaimDialog = false },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StrokeGray)
                ) { Text("Cancel", color = TextPrimary) }
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = PaperWhite
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Item Details", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PaperWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PaperWhite)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (item.isSaved) viewModel.unsaveItem(item.id)
                        else viewModel.saveItem(item.id)
                    }) {
                        Icon(
                            if (item.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save",
                            tint = PaperWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Charcoal)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = IvoryWhite
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())
        ) {
            // Hero — show image if available, else emoji
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Charcoal, Slate)))
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(84.dp).clip(RoundedCornerShape(22.dp))
                            .background(PaperWhite.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.imageUrl != null) {
                            AsyncImage(
                                model = item.imageUrl,
                                contentDescription = item.title,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(22.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(categoryEmoji(item.title), fontSize = 44.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(20.dp))
                            .background(if (isLost) LostRedBg else FoundGreenBg)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            if (item.isResolved) "RESOLVED" else item.status.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = if (isLost) LostRed else FoundGreen
                        )
                    }
                }
            }

            // Content card
            Surface(
                modifier = Modifier.fillMaxWidth().offset(y = (-16).dp),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                color = IvoryWhite
            ) {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {

                    Text(item.title, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(item.reportedAt, fontSize = 12.sp, color = TextSecond)

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = StrokeGray)
                    Spacer(modifier = Modifier.height(20.dp))

                    DetailInfoRow(Icons.Default.LocationOn, "Location", item.location.ifBlank { "Not specified" })
                    DetailInfoRow(Icons.Default.CalendarToday, "Date Reported", item.reportedAt.ifBlank { "Unknown" })
                    DetailInfoRow(Icons.Default.Category, "Category", item.contactInfo.ifBlank { "General" })

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = StrokeGray)
                    Spacer(modifier = Modifier.height(20.dp))

                    Text("Description", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(12.dp), color = PaperWhite, shadowElevation = 1.dp) {
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

                    Surface(
                        shape = RoundedCornerShape(14.dp), color = PaperWhite,
                        shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(50.dp)
                                        .background(Brush.linearGradient(listOf(Charcoal, Slate)), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        item.reporterName.take(2).uppercase(),
                                        color = PaperWhite, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.reporterName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextPrimary)
                                    Text(item.reporterDept, fontSize = 12.sp, color = TextSecond)
                                }
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(6.dp))
                                        .background(FoundGreenBg).padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Verified", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FoundGreen)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = StrokeGray)
                            Spacer(modifier = Modifier.height(12.dp))
                            if (item.reporterEmail.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Email, contentDescription = null, tint = TextSecond, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(item.reporterEmail, fontSize = 13.sp, color = TextPrimary)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            if (item.reporterPhone.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecond, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(item.reporterPhone, fontSize = 13.sp, color = TextPrimary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (!item.isResolved) {
                        Button(
                            onClick = { showClaimDialog = true },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = PaperWhite),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Icon(
                                if (isLost) Icons.Default.Search else Icons.Default.CheckCircle,
                                contentDescription = null, modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isLost) "I Found This Item" else "This is Mine",
                                fontWeight = FontWeight.Bold, fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedButton(
                        onClick = { showContact = !showContact },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Charcoal),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StrokeGray)
                    ) {
                        Icon(Icons.Default.ContactPhone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (showContact) "Hide Contact" else "Contact Reporter",
                            fontWeight = FontWeight.SemiBold, fontSize = 14.sp
                        )
                    }

                    AnimatedVisibility(
                        visible = showContact,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            shape = RoundedCornerShape(12.dp), color = PaperWhite, shadowElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Contact directly:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                                Spacer(modifier = Modifier.height(6.dp))
                                if (item.reporterEmail.isNotBlank()) Text(item.reporterEmail, fontSize = 13.sp, color = TextSecond)
                                if (item.reporterPhone.isNotBlank()) Text(item.reporterPhone, fontSize = 13.sp, color = TextSecond)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun DetailStatusBadge(status: ItemStatus) {
    val isLost = status == ItemStatus.LOST
    Box(
        modifier = Modifier.clip(RoundedCornerShape(6.dp))
            .background(if (isLost) LostRedBg else FoundGreenBg)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(status.name, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = if (isLost) LostRed else FoundGreen)
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = TextSecond, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextSecond)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        }
    }
}

@Composable
private fun DetailInfoRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
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
