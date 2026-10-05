package com.example.lostandfoundfrontend.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lostandfoundfrontend.R
import com.example.lostandfoundfrontend.data.LostFoundViewModel
import com.example.lostandfoundfrontend.ui.components.bounceClick
import com.example.lostandfoundfrontend.ui.theme.*
import kotlinx.coroutines.launch

/** Snackbar host that shows the ViewModel's one-shot messages. */
@Composable
fun rememberToastHost(viewModel: LostFoundViewModel): SnackbarHostState {
    val host = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.toasts.collect { message ->
            host.currentSnackbarData?.dismiss()
            launch { host.showSnackbar(message) }
        }
    }
    return host
}

@Composable
fun AppSnackbarHost(host: SnackbarHostState) {
    SnackbarHost(host) { data ->
        Snackbar(
            snackbarData = data,
            containerColor = Charcoal,
            contentColor = PaperWhite,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun TopHeaderBar(title: String = "Campus Lost & Found", subtitle: String? = null) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.horizontalGradient(listOf(Charcoal, Slate)))
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Logo circle
            Box(
                modifier = Modifier.size(38.dp)
                    .background(PaperWhite.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = R.drawable.search,
                    contentDescription = "Logo",
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = PaperWhite)
                if (subtitle != null) {
                    Text(subtitle, fontSize = 11.sp, color = PaperWhite.copy(alpha = 0.6f))
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    selectedScreen: String,
    onHomeClick: () -> Unit,
    onReportClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PaperWhite,
        shadowElevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    icon = Icons.Default.Home, label = "Home",
                    isSelected = selectedScreen == "Home",
                    modifier = Modifier.weight(1f), onClick = onHomeClick
                )
                Spacer(modifier = Modifier.weight(1f)) // space for FAB
                NavItem(
                    icon = Icons.Default.Person, label = "Profile",
                    isSelected = selectedScreen == "Profile",
                    modifier = Modifier.weight(1f), onClick = onProfileClick
                )
            }
            // Centre FAB
            Box(
                modifier = Modifier.align(Alignment.TopCenter).offset(y = (-18).dp),
                contentAlignment = Alignment.Center
            ) {
                // Pops in when the bar first appears, then springs on press
                val appear = remember { Animatable(0f) }
                LaunchedEffect(Unit) { appear.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessLow)) }
                val isReport = selectedScreen == "Report"
                val rotation by animateFloatAsState(if (isReport) 45f else 0f, spring(dampingRatio = 0.6f), label = "fabRotation")
                Box(
                    modifier = Modifier
                        .graphicsLayer { scaleX = appear.value; scaleY = appear.value }
                        .size(56.dp)
                        .shadow(8.dp, CircleShape)
                        .background(
                            Brush.linearGradient(listOf(Charcoal, Slate)),
                            CircleShape
                        )
                        .bounceClick { onReportClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add, contentDescription = "Report", tint = PaperWhite,
                        modifier = Modifier.size(26.dp).graphicsLayer { rotationZ = rotation }
                    )
                }
            }
        }
    }
}

@Composable
fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.5f), label = "scale"
    )
    val color by animateColorAsState(
        targetValue = if (isSelected) Charcoal else TextHint, label = "color"
    )

    Column(
        modifier = modifier.fillMaxHeight().clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp).scale(scale))
        Spacer(modifier = Modifier.height(3.dp))
        Text(label, fontSize = 10.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = color)
        if (isSelected) {
            Spacer(modifier = Modifier.height(3.dp))
            Box(modifier = Modifier.size(4.dp).background(Charcoal, CircleShape))
        }
    }
}
