package com.example.lostandfoundfrontend.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lostandfoundfrontend.data.LostFoundViewModel
import com.example.lostandfoundfrontend.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: LostFoundViewModel,
    onLogout: () -> Unit,
    onMyReports: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onDarkMode: () -> Unit = {}
) {
    val profileState by viewModel.profileState.collectAsState()
    val user = profileState.user

    var showLogoutDialog by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
        visible = true
    }

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
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StrokeGray)
                ) { Text("Cancel", color = TextPrimary) }
            },
            shape = RoundedCornerShape(18.dp),
            containerColor = PaperWhite
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(IvoryWhite).verticalScroll(rememberScrollState())) {

        // Gradient header
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Charcoal, Slate)))
                .padding(horizontal = 24.dp, vertical = 36.dp),
            contentAlignment = Alignment.Center
        ) {
            if (profileState.isLoading) {
                CircularProgressIndicator(color = PaperWhite, modifier = Modifier.size(32.dp))
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(80.dp).background(PaperWhite.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user?.avatarUrl != null) {
                            AsyncImage(
                                model = user.avatarUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier.size(68.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.size(68.dp).background(PaperWhite.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(initials, color = PaperWhite, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(user?.name ?: "Loading...", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PaperWhite)
                    Text(user?.email ?: "", fontSize = 13.sp, color = PaperWhite.copy(alpha = 0.65f))
                    if (user?.studentClass != null || user?.department != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp))
                                .background(PaperWhite.copy(alpha = 0.12f))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                listOfNotNull(user.studentClass, user.department).joinToString("  •  "),
                                fontSize = 12.sp, color = PaperWhite.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }

        // Stats card
        AnimatedVisibility(visible = visible, enter = slideInVertically { it / 2 } + fadeIn()) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).offset(y = (-16).dp),
                shape = RoundedCornerShape(16.dp),
                color = PaperWhite,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStatItem("${user?.reportsCount ?: 0}", "Reported")
                    Box(modifier = Modifier.width(1.dp).height(36.dp).background(StrokeGray))
                    ProfileStatItem("${user?.resolvedCount ?: 0}", "Resolved")
                    Box(modifier = Modifier.width(1.dp).height(36.dp).background(StrokeGray))
                    ProfileStatItem("${user?.pendingCount ?: 0}", "Pending")
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {

            ProfileSectionTitle("Account")
            ProfileGroup {
                ProfileRow(icon = Icons.Default.Person, label = "Edit Profile", onClick = onEditProfile)
                ProfileDivider()
                ProfileRow(icon = Icons.Default.Lock, label = "Change Password") {}
                ProfileDivider()
                ProfileRow(icon = Icons.Default.Phone, label = "Update Mobile") {}
            }

            Spacer(modifier = Modifier.height(18.dp))
            ProfileSectionTitle("Activity")
            ProfileGroup {
                ProfileRow(icon = Icons.Default.List, label = "My Reports", onClick = onMyReports)
                ProfileDivider()
                ProfileRow(icon = Icons.Default.Bookmark, label = "Saved Items") {}
                ProfileDivider()
                ProfileRow(icon = Icons.Default.History, label = "Activity History") {}
            }

            Spacer(modifier = Modifier.height(18.dp))
            ProfileSectionTitle("Preferences")
            ProfileGroup {
                ProfileRow(icon = Icons.Default.Notifications, label = "Notifications") {}
                ProfileDivider()
                ProfileRow(icon = Icons.Default.DarkMode, label = "Dark Mode", onClick = onDarkMode)
                ProfileDivider()
                ProfileRow(icon = Icons.Default.Language, label = "Language") {}
            }

            Spacer(modifier = Modifier.height(18.dp))
            ProfileSectionTitle("Support")
            ProfileGroup {
                ProfileRow(icon = Icons.Default.Help, label = "Help & FAQ") {}
                ProfileDivider()
                ProfileRow(icon = Icons.Default.Info, label = "About App") {}
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LostRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, LostRed.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
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
private fun ProfileRow(icon: ImageVector, label: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(9.dp)).background(SurfaceGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Charcoal, modifier = Modifier.size(17.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(label, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
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
