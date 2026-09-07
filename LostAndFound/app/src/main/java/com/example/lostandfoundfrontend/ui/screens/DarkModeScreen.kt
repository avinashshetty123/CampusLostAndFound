package com.example.lostandfoundfrontend.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lostandfoundfrontend.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DarkModeScreen(onBack: () -> Unit) {
    var darkEnabled by remember { mutableStateOf(false) }
    var systemDefault by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Appearance", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PaperWhite) },
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
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            // Preview box
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp)
                    .background(Brush.verticalGradient(listOf(Charcoal, Slate)))
                    .padding(24.dp)
            ) {
                Column {
                    Text("Preview", fontSize = 12.sp, color = PaperWhite.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))
                    // Mini mock card
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (darkEnabled) Color(0xFF2A2A3E) else PaperWhite)
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                                    .background(if (darkEnabled) Color(0xFF3A3A50) else SurfaceGray),
                                contentAlignment = Alignment.Center
                            ) { Text("💻", fontSize = 20.sp) }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("MacBook Air", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (darkEnabled) PaperWhite else TextPrimary)
                                Text("Library - 3rd Floor", fontSize = 11.sp, color = if (darkEnabled) TextHint else TextSecond)
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Theme Settings", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextSecond)

                // System default toggle
                Surface(shape = RoundedCornerShape(14.dp), color = PaperWhite, shadowElevation = 2.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = TextSecond, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Use System Default", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                            Text("Follow your device theme", fontSize = 12.sp, color = TextSecond)
                        }
                        Switch(
                            checked = systemDefault,
                            onCheckedChange = { systemDefault = it; if (it) darkEnabled = false },
                            colors = SwitchDefaults.colors(checkedThumbColor = PaperWhite, checkedTrackColor = Charcoal)
                        )
                    }
                }

                // Dark mode toggle
                Surface(shape = RoundedCornerShape(14.dp), color = PaperWhite, shadowElevation = 2.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DarkMode, contentDescription = null, tint = TextSecond, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dark Mode", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                            Text("Easier on the eyes at night", fontSize = 12.sp, color = TextSecond)
                        }
                        Switch(
                            checked = darkEnabled,
                            onCheckedChange = { darkEnabled = it; if (it) systemDefault = false },
                            enabled = !systemDefault,
                            colors = SwitchDefaults.colors(checkedThumbColor = PaperWhite, checkedTrackColor = Charcoal)
                        )
                    }
                }

                // Light mode option
                Surface(shape = RoundedCornerShape(14.dp), color = PaperWhite, shadowElevation = 2.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LightMode, contentDescription = null, tint = TextSecond, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Light Mode", fontWeight = FontWeight.Medium, fontSize = 14.sp, color = TextPrimary)
                            Text("Classic bright appearance", fontSize = 12.sp, color = TextSecond)
                        }
                        RadioButton(
                            selected = !darkEnabled && !systemDefault,
                            onClick = { darkEnabled = false; systemDefault = false },
                            colors = RadioButtonDefaults.colors(selectedColor = Charcoal)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Note: Full dark mode support will be available in the next update.",
                    fontSize = 12.sp, color = TextHint
                )
            }
        }
    }
}
