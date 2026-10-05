package com.example.lostandfoundfrontend.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.AsyncImage
import com.example.lostandfoundfrontend.data.ImageUtils
import com.example.lostandfoundfrontend.ui.components.bounceClick
import kotlinx.coroutines.launch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lostandfoundfrontend.data.LostFoundViewModel
import com.example.lostandfoundfrontend.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(viewModel: LostFoundViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
    val profileState by viewModel.profileState.collectAsState()
    val user = profileState.user

    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var email by remember(user) { mutableStateOf(user?.email ?: "") }
    var mobile by remember(user) { mutableStateOf(user?.mobile ?: "") }
    var studentClass by remember(user) { mutableStateOf(user?.studentClass ?: "") }
    var department by remember(user) { mutableStateOf(user?.department ?: "") }

    val snackbarHostState = rememberToastHost(viewModel)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) scope.launch {
            ImageUtils.prepareForUpload(context, uri)?.let { viewModel.uploadAvatar(it) }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PaperWhite) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PaperWhite)
                    }
                },
                actions = {
                    TextButton(
                        enabled = !profileState.isSaving,
                        onClick = { viewModel.updateProfile(name, mobile, studentClass, department, onSaved) }
                    ) {
                        Text("Save", color = PaperWhite, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Charcoal)
            )
        },
        snackbarHost = { AppSnackbarHost(snackbarHostState) },
        containerColor = IvoryWhite
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {

            // Avatar section
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Charcoal, Slate)))
                    .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier.bounceClick(enabled = !profileState.isUploadingAvatar) { avatarPicker.launch("image/*") }
                    ) {
                        Box(
                            modifier = Modifier.size(80.dp).clip(CircleShape).background(PaperWhite.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (user?.avatarUrl != null) {
                                AsyncImage(
                                    model = user.avatarUrl, contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    name.split(" ").mapNotNull { it.firstOrNull()?.uppercaseChar() }.take(2).joinToString(""),
                                    color = PaperWhite, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp
                                )
                            }
                            // Spinner overlay while the new photo uploads
                            androidx.compose.animation.AnimatedVisibility(
                                visible = profileState.isUploadingAvatar,
                                enter = androidx.compose.animation.fadeIn(), exit = androidx.compose.animation.fadeOut()
                            ) {
                                Box(Modifier.fillMaxSize().background(Charcoal.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = PaperWhite, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                                }
                            }
                        }
                        Box(
                            modifier = Modifier.size(26.dp).clip(CircleShape).background(PaperWhite),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Charcoal, modifier = Modifier.size(14.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Tap to change photo", fontSize = 12.sp, color = PaperWhite.copy(alpha = 0.6f))
                }
            }

            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                EditSectionLabel("Personal Info")
                EditField(value = name, onValueChange = { name = it }, label = "Full Name", icon = Icons.Default.Person)
                EditField(value = email, onValueChange = {}, label = "Email Address", icon = Icons.Default.Email, readOnly = true)
                EditField(value = mobile, onValueChange = { mobile = it }, label = "Mobile Number", icon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone)

                Spacer(modifier = Modifier.height(4.dp))
                EditSectionLabel("Academic Info")
                EditField(value = studentClass, onValueChange = { studentClass = it }, label = "Class / Year", icon = Icons.Default.School)
                EditField(value = department, onValueChange = { department = it }, label = "Department", icon = Icons.Default.Business)

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.updateProfile(name, mobile, studentClass, department, onSaved) },
                    enabled = !profileState.isSaving,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = PaperWhite),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    if (profileState.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PaperWhite, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun EditSectionLabel(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextSecond)
}

@Composable
private fun EditField(
    value: String, onValueChange: (String) -> Unit, label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    readOnly: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        leadingIcon = { Icon(icon, contentDescription = null, tint = TextSecond, modifier = Modifier.size(18.dp)) },
        readOnly = readOnly,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Charcoal, unfocusedBorderColor = StrokeGray,
            focusedContainerColor = PaperWhite, unfocusedContainerColor = PaperWhite,
            focusedLabelColor = Charcoal, unfocusedLabelColor = TextSecond,
            disabledBorderColor = StrokeGray, disabledContainerColor = SurfaceGray
        ),
        singleLine = true
    )
}
