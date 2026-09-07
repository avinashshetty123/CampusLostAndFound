package com.example.lostandfoundfrontend.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lostandfoundfrontend.data.LostFoundViewModel
import com.example.lostandfoundfrontend.model.Item
import com.example.lostandfoundfrontend.model.ItemStatus
import com.example.lostandfoundfrontend.ui.theme.*
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: LostFoundViewModel,
    onItemReported: (Item) -> Unit,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current
    val isLoading by viewModel.reportLoading.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(ItemStatus.LOST) }
    var titleError by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            snackbarHostState.showSnackbar(toastMessage!!)
            viewModel.clearToast()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedScreen = "Report",
                onHomeClick = onHomeClick,
                onReportClick = {},
                onProfileClick = onProfileClick
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = IvoryWhite
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())
        ) {
            TopHeaderBar(subtitle = "Report a lost or found item")

            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Charcoal, Slate)))
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Column {
                    Text(
                        if (status == ItemStatus.LOST) "Report Lost Item" else "Report Found Item",
                        fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = PaperWhite
                    )
                    Text("Help the campus community find it", fontSize = 13.sp, color = PaperWhite.copy(alpha = 0.65f))
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {

                // Item Type
                FormSectionLabel("Item Type")
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ItemStatus.values().forEach { itemStatus ->
                        val selected = status == itemStatus
                        Surface(
                            modifier = Modifier.weight(1f).height(52.dp).clickable { status = itemStatus },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) Charcoal else PaperWhite,
                            shadowElevation = if (selected) 4.dp else 1.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (itemStatus == ItemStatus.LOST) Icons.Default.SearchOff else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (selected) PaperWhite else TextSecond,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    itemStatus.name,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) PaperWhite else TextSecond,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Item Details
                FormSectionLabel("Item Details")
                Spacer(modifier = Modifier.height(10.dp))
                InputField(label = "Item Name *", placeholder = "e.g. MacBook Air, Blue Backpack", value = title, isError = titleError) {
                    title = it; titleError = false
                }
                InputField(label = "Location", placeholder = "e.g. Library 3rd Floor, Cafeteria", value = location) { location = it }
                InputField(label = "Category", placeholder = "e.g. Electronics, Accessories, Books", value = category) { category = it }
                InputField(label = "Contact Info", placeholder = "Phone or email to reach you", value = contactInfo) { contactInfo = it }
                InputField(
                    label = "Description",
                    placeholder = "Color, brand, distinctive features...",
                    value = description,
                    singleLine = false,
                    modifier = Modifier.height(110.dp)
                ) { description = it }

                Spacer(modifier = Modifier.height(4.dp))

                // Photo Upload
                FormSectionLabel("Item Photo (Optional)")
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (selectedImageUri != null) Color.Transparent else SurfaceGray)
                        .clickable { photoPickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected photo",
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier.fillMaxSize().background(Charcoal.copy(alpha = 0.35f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = PaperWhite, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Change Photo", color = PaperWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(StrokeGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = TextSecond, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap to upload photo", fontSize = 13.sp, color = TextSecond, fontWeight = FontWeight.Medium)
                            Text("JPG, PNG up to 10MB", fontSize = 11.sp, color = TextHint)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            titleError = true
                        } else {
                            val imageFile = selectedImageUri?.toFile(context)
                            viewModel.reportItem(title, description, location, status, category, contactInfo, imageFile) { item ->
                                onItemReported(item)
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = PaperWhite),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PaperWhite, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Submit Report", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private fun Uri.toFile(context: Context): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(this) ?: return null
        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
        FileOutputStream(tempFile).use { out -> inputStream.copyTo(out) }
        tempFile
    } catch (e: Exception) {
        null
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
    isError: Boolean = false, accentColor: androidx.compose.ui.graphics.Color = Charcoal,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 14.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextHint, fontSize = 13.sp) },
            singleLine = singleLine, isError = isError,
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Charcoal, unfocusedBorderColor = StrokeGray,
                focusedContainerColor = PaperWhite, unfocusedContainerColor = PaperWhite,
                errorBorderColor = LostRed
            )
        )
        if (isError) {
            Text("This field is required", color = LostRed, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp, top = 2.dp))
        }
    }
}
