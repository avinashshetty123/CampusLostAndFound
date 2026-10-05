package com.example.lostandfoundfrontend.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lostandfoundfrontend.data.LostFoundViewModel
import com.example.lostandfoundfrontend.ui.components.animatedCount
import com.example.lostandfoundfrontend.ui.components.shimmer
import com.example.lostandfoundfrontend.ui.components.staggeredEntrance
import com.example.lostandfoundfrontend.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: LostFoundViewModel,
    onLogout: () -> Unit,
    onMyReports: () -> Unit = {},
    onSavedItems: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onReportClick: () -> Unit = {}
) {
    val profileState by viewModel.profileState.collectAsState()
    val user = profileState.user
    val snackbarHost = rememberToastHost(viewModel)

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadProfile() }

    val initials = user?.name?.split(" ")
        ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
        ?.take(2)?.joinToString("") ?: "?"

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                Button(
                    onClick = { showLogoutDialog = false; onLogout() },
                    colors = ButtonDefaults.buttonColors(containerColor = LostRed),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Log Out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel", color = TextPrimary) }
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = PaperWhite
        )
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onSubmit = { current, new -> viewModel.changePassword(current, new) { showPasswordDialog = false } }
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            icon = { Icon(Icons.Default.Info, contentDescription = null, tint = Charcoal) },
            title = { Text("Campus Lost & Found", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Report items you've lost or found on campus, browse what others have posted, " +
                        "and claim your belongings. Reporters see your claim message and can contact you directly.",
                    fontSize = 14.sp, color = TextSecond
                )
            },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("Close", color = Charcoal) } },
            shape = RoundedCornerShape(18.dp),
            containerColor = PaperWhite
        )
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedScreen = "Profile",
                onHomeClick = onHomeClick,
                onReportClick = onReportClick,
                onProfileClick = {}
            )
        },
        snackbarHost = { AppSnackbarHost(snackbarHost) },
        containerColor = IvoryWhite
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())) {

            // Gradient header
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Charcoal, Slate)))
                    .padding(horizontal = 24.dp, vertical = 36.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(84.dp).staggeredEntrance(0)
                            .background(PaperWhite.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Crossfade(targetState = user?.avatarUrl, label = "avatar") { avatarUrl ->
                            if (avatarUrl != null) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.size(72.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier.size(72.dp).background(PaperWhite.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(initials, color = PaperWhite, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    if (user == null && profileState.isLoading) {
                        Box(Modifier.width(140.dp).height(20.dp).clip(RoundedCornerShape(6.dp)).shimmer())
                        Spacer(Modifier.height(6.dp))
                        Box(Modifier.width(180.dp).height(12.dp).clip(RoundedCornerShape(6.dp)).shimmer())
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.staggeredEntrance(1)) {
                            Text(user?.name ?: "", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PaperWhite)
                            Text(user?.email ?: "", fontSize = 13.sp, color = PaperWhite.copy(alpha = 0.65f))
                            val academic = listOfNotNull(user?.studentClass, user?.department).filter { it.isNotBlank() }
                            if (academic.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                        .background(PaperWhite.copy(alpha = 0.12f))
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Text(academic.joinToString("  •  "), fontSize = 12.sp, color = PaperWhite.copy(alpha = 0.85f))
                                }
                            }
                        }
                    }
                    if (profileState.error != null && user == null) {
                        Spacer(Modifier.height(8.dp))
                        Text(profileState.error ?: "", color = PaperWhite.copy(alpha = 0.8f), fontSize = 12.sp, textAlign = TextAlign.Center)
                        TextButton(onClick = { viewModel.loadProfile() }) { Text("Retry", color = PaperWhite) }
                    }
                }
            }

            // Stats card — numbers count up
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).offset(y = (-16).dp).staggeredEntrance(2, offsetY = 40.dp),
                shape = RoundedCornerShape(16.dp),
                color = PaperWhite,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStatItem(animatedCount((user?.reportsCount ?: 0).toLong()), "Reported")
                    Box(modifier = Modifier.width(1.dp).height(36.dp).background(StrokeGray))
                    ProfileStatItem(animatedCount((user?.resolvedCount ?: 0).toLong()), "Resolved")
                    Box(modifier = Modifier.width(1.dp).height(36.dp).background(StrokeGray))
                    ProfileStatItem(animatedCount((user?.pendingCount ?: 0).toLong()), "Open")
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                Column(Modifier.staggeredEntrance(3)) {
                    ProfileSectionTitle("Account")
                    ProfileGroup {
                        ProfileRow(icon = Icons.Default.Person, label = "Edit Profile", onClick = onEditProfile)
                        ProfileDivider()
                        ProfileRow(icon = Icons.Default.Lock, label = "Change Password") { showPasswordDialog = true }
                        ProfileDivider()
                        ProfileRow(icon = Icons.Default.Phone, label = "Update Mobile", value = user?.mobile, onClick = onEditProfile)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Column(Modifier.staggeredEntrance(4)) {
                    ProfileSectionTitle("Activity")
                    ProfileGroup {
                        ProfileRow(icon = Icons.AutoMirrored.Filled.List, label = "My Reports", onClick = onMyReports)
                        ProfileDivider()
                        ProfileRow(icon = Icons.Default.Bookmark, label = "Saved Items", onClick = onSavedItems)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Column(Modifier.staggeredEntrance(5)) {
                    ProfileSectionTitle("Preferences")
                    ProfileGroup {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RowIcon(Icons.Default.Notifications)
                            Spacer(modifier = Modifier.width(14.dp))
                            Text("Notifications", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                            Switch(
                                checked = user?.notificationsEnabled ?: true,
                                onCheckedChange = { viewModel.updateNotifications(it) },
                                enabled = user != null,
                                colors = SwitchDefaults.colors(checkedThumbColor = PaperWhite, checkedTrackColor = Charcoal)
                            )
                        }
                        ProfileDivider()
                        ProfileRow(icon = Icons.Default.Info, label = "About App") { showAbout = true }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp).staggeredEntrance(6),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = LostRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LostRed.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Campus Lost & Found  v1.0", fontSize = 11.sp, color = TextHint,
                    modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun ChangePasswordDialog(onDismiss: () -> Unit, onSubmit: (String, String) -> Unit) {
    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val mismatch = confirm.isNotEmpty() && confirm != new
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PasswordInput(current, { current = it }, "Current password")
                PasswordInput(new, { new = it }, "New password (min 6)")
                PasswordInput(confirm, { confirm = it }, "Confirm new password", isError = mismatch)
                if (mismatch) Text("Passwords don't match", color = LostRed, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(current, new) },
                enabled = current.isNotEmpty() && new.length >= 6 && confirm == new,
                colors = ButtonDefaults.buttonColors(containerColor = Charcoal),
                shape = RoundedCornerShape(10.dp)
            ) { Text("Update") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextPrimary) } },
        shape = RoundedCornerShape(18.dp),
        containerColor = PaperWhite
    )
}

@Composable
private fun PasswordInput(value: String, onChange: (String) -> Unit, label: String, isError: Boolean = false) {
    OutlinedTextField(
        value = value, onValueChange = onChange,
        label = { Text(label, fontSize = 13.sp) },
        singleLine = true, isError = isError,
        visualTransformation = PasswordVisualTransformation(),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Charcoal, unfocusedBorderColor = StrokeGray, focusedLabelColor = Charcoal),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ProfileSectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecond,
        modifier = Modifier.padding(bottom = 8.dp, start = 2.dp))
}

@Composable
private fun ProfileGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = PaperWhite, shadowElevation = 2.dp) {
        Column(content = content)
    }
}

@Composable
private fun ProfileDivider() {
    HorizontalDivider(color = StrokeGray, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun RowIcon(icon: ImageVector) {
    Box(
        modifier = Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(SurfaceGray),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Charcoal, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, value: String? = null, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowIcon(icon)
        Spacer(modifier = Modifier.width(14.dp))
        Text(label, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
        if (!value.isNullOrBlank()) {
            Text(value, fontSize = 12.sp, color = TextSecond)
            Spacer(Modifier.width(6.dp))
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextHint, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun ProfileStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Charcoal)
        Text(label, fontSize = 11.sp, color = TextSecond)
    }
}
