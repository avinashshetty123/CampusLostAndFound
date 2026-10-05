package com.example.lostandfoundfrontend.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lostandfoundfrontend.data.ImageUtils
import com.example.lostandfoundfrontend.data.LostFoundViewModel
import com.example.lostandfoundfrontend.model.Item
import com.example.lostandfoundfrontend.model.ItemStatus
import com.example.lostandfoundfrontend.ui.components.SuccessOverlay
import com.example.lostandfoundfrontend.ui.components.bounceClick
import com.example.lostandfoundfrontend.ui.components.shake
import com.example.lostandfoundfrontend.ui.components.staggeredEntrance
import com.example.lostandfoundfrontend.ui.theme.*
import kotlinx.coroutines.launch

private val CategorySuggestions = listOf("Electronics", "Bags", "Wallets & Cards", "Keys", "Books", "Clothing", "Accessories", "Other")

@Composable
fun ReportScreen(
    viewModel: LostFoundViewModel,
    onItemReported: (Item) -> Unit,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isSubmitting by viewModel.reportLoading.collectAsState()
    val snackbarHost = rememberToastHost(viewModel)

    var title by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var contactInfo by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf(ItemStatus.LOST) }
    var titleErrorCount by remember { mutableIntStateOf(0) }
    var selectedImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var preparingImage by remember { mutableStateOf(false) }
    var reportedItem by remember { mutableStateOf<Item?>(null) }

    val isLoading = isSubmitting || preparingImage

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> if (uri != null) selectedImageUri = uri }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    selectedScreen = "Report",
                    onHomeClick = onHomeClick,
                    onReportClick = {},
                    onProfileClick = onProfileClick
                )
            },
            snackbarHost = { AppSnackbarHost(snackbarHost) },
            containerColor = IvoryWhite
        ) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).imePadding().verticalScroll(rememberScrollState())
            ) {
                TopHeaderBar(subtitle = "Report a lost or found item")

                val headerBrush = if (status == ItemStatus.LOST) listOf(Charcoal, LostRed.copy(alpha = 0.55f)) else listOf(Charcoal, FoundGreen.copy(alpha = 0.6f))
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.horizontalGradient(headerBrush))
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Column {
                        AnimatedContent(
                            targetState = status,
                            transitionSpec = { (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut()) },
                            label = "reportTitle"
                        ) { s ->
                            Text(
                                if (s == ItemStatus.LOST) "Report Lost Item" else "Report Found Item",
                                fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = PaperWhite
                            )
                        }
                        Text("Help the campus community find it", fontSize = 13.sp, color = PaperWhite.copy(alpha = 0.7f))
                    }
                }

                Column(modifier = Modifier.padding(20.dp)) {

                    // Item Type
                    FormSectionLabel("Item Type")
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().staggeredEntrance(0),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ItemStatus.entries.forEach { itemStatus ->
                            StatusOption(
                                status = itemStatus,
                                selected = status == itemStatus,
                                modifier = Modifier.weight(1f)
                            ) { status = itemStatus }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Item Details
                    FormSectionLabel("Item Details")
                    Spacer(modifier = Modifier.height(10.dp))
                    InputField(
                        label = "Item Name *", placeholder = "e.g. MacBook Air, Blue Backpack", value = title,
                        isError = titleErrorCount > 0 && title.isBlank(),
                        modifier = Modifier.shake(titleErrorCount.takeIf { it > 0 }),
                        containerModifier = Modifier.staggeredEntrance(1)
                    ) { title = it }
                    InputField(
                        label = if (status == ItemStatus.LOST) "Where did you lose it?" else "Where did you find it?",
                        placeholder = "e.g. Library 3rd Floor, Cafeteria", value = location,
                        containerModifier = Modifier.staggeredEntrance(2)
                    ) { location = it }

                    // Category quick-picks
                    Column(Modifier.padding(bottom = 14.dp).staggeredEntrance(3)) {
                        Text("Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Spacer(Modifier.height(6.dp))
                        FlowRowCompat(CategorySuggestions, selected = category) { picked ->
                            category = if (category == picked) "" else picked
                        }
                    }

                    InputField(
                        label = "Contact Info", placeholder = "Phone or email to reach you", value = contactInfo,
                        containerModifier = Modifier.staggeredEntrance(4)
                    ) { contactInfo = it }
                    InputField(
                        label = "Description",
                        placeholder = "Color, brand, distinctive features...",
                        value = description,
                        singleLine = false,
                        modifier = Modifier.heightIn(min = 110.dp),
                        containerModifier = Modifier.staggeredEntrance(5)
                    ) { description = it }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Photo Upload
                    FormSectionLabel("Item Photo (Optional)")
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                            .staggeredEntrance(6)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceGray)
                            .clickable(enabled = !isLoading) { photoPickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(
                            targetState = selectedImageUri,
                            transitionSpec = { (fadeIn(tween(300)) + scaleIn(initialScale = 0.92f)) togetherWith fadeOut(tween(200)) },
                            label = "photo"
                        ) { uri ->
                            if (uri != null) {
                                Box(Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Selected photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(Charcoal.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Edit, contentDescription = null, tint = PaperWhite, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Change Photo", color = PaperWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        }
                                    }
                                    IconButton(
                                        onClick = { selectedImageUri = null },
                                        modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(30.dp)
                                            .background(Charcoal.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove photo", tint = PaperWhite, modifier = Modifier.size(16.dp))
                                    }
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Box(
                                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(StrokeGray),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = TextSecond, modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Tap to upload photo", fontSize = 13.sp, color = TextSecond, fontWeight = FontWeight.Medium)
                                    Text("A photo makes it much easier to identify", fontSize = 11.sp, color = TextHint)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    PrimaryButton(
                        text = "Submit Report",
                        loading = isLoading,
                        icon = Icons.AutoMirrored.Filled.Send,
                        modifier = Modifier.staggeredEntrance(7)
                    ) {
                        if (title.isBlank()) {
                            titleErrorCount++
                            return@PrimaryButton
                        }
                        scope.launch {
                            val imageFile = selectedImageUri?.let {
                                preparingImage = true
                                ImageUtils.prepareForUpload(context, it).also { preparingImage = false }
                            }
                            viewModel.reportItem(title, description, location, status, category, contactInfo, imageFile) { item ->
                                reportedItem = item
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        SuccessOverlay(
            visible = reportedItem != null,
            title = "Report submitted!",
            subtitle = if (status == ItemStatus.LOST) "We hope you find it soon. Others on campus can now see it."
            else "Thanks for helping! The owner can now find it.",
            onFinished = { reportedItem?.let(onItemReported) }
        )
    }
}

@Composable
private fun StatusOption(status: ItemStatus, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val accent = if (status == ItemStatus.LOST) LostRed else FoundGreen
    val bg by animateColorAsState(if (selected) Charcoal else PaperWhite, label = "statusBg")
    val fg by animateColorAsState(if (selected) PaperWhite else TextSecond, label = "statusFg")
    val border by animateColorAsState(if (selected) accent else StrokeGray, label = "statusBorder")
    Surface(
        modifier = modifier.height(52.dp).bounceClick(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = androidx.compose.foundation.BorderStroke(if (selected) 2.dp else 1.dp, border),
        shadowElevation = if (selected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (status == ItemStatus.LOST) Icons.Default.SearchOff else Icons.Default.CheckCircle,
                contentDescription = null, tint = fg, modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(status.name, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, color = fg, fontSize = 14.sp)
        }
    }
}

/** Simple wrapping row of selectable chips (no experimental FlowRow needed). */
@Composable
private fun FlowRowCompat(options: List<String>, selected: String, onPick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(3).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { option ->
                    val isSelected = option == selected
                    val bg by animateColorAsState(if (isSelected) Charcoal else PaperWhite, label = "chipBg")
                    val fg by animateColorAsState(if (isSelected) PaperWhite else TextSecond, label = "chipFg")
                    Box(
                        Modifier.clip(RoundedCornerShape(18.dp))
                            .background(bg)
                            .bounceClick { onPick(option) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(option, fontSize = 12.sp, color = fg, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
            }
        }
    }
}

@Composable
private fun FormSectionLabel(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
}

@Composable
fun InputField(
    label: String, placeholder: String, value: String,
    singleLine: Boolean = true, modifier: Modifier = Modifier,
    containerModifier: Modifier = Modifier,
    isError: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Column(modifier = containerModifier.padding(bottom = 14.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextHint, fontSize = 13.sp) },
            singleLine = singleLine, isError = isError,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Charcoal, unfocusedBorderColor = StrokeGray,
                focusedContainerColor = PaperWhite, unfocusedContainerColor = PaperWhite,
                errorBorderColor = LostRed, errorContainerColor = PaperWhite
            )
        )
        AnimatedVisibility(visible = isError, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Text("This field is required", color = LostRed, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
        }
    }
}
