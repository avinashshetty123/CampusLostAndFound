package com.example.lostandfoundfrontend.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lostandfoundfrontend.R
import com.example.lostandfoundfrontend.ui.theme.*

@Composable
fun EntryScreen(onGetStarted: () -> Unit) {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val logoScale by pulse.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "logoScale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Top gradient hero
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .background(
                    Brush.verticalGradient(listOf(Charcoal, Slate, SlateLight))
                )
        ) {
            // Subtle circle decorations
            Box(
                modifier = Modifier.size(260.dp).offset(x = (-60).dp, y = (-60).dp)
                    .background(PaperWhite.copy(alpha = 0.04f), CircleShape)
            )
            Box(
                modifier = Modifier.size(180.dp).align(Alignment.BottomEnd).offset(x = 50.dp, y = 50.dp)
                    .background(PaperWhite.copy(alpha = 0.04f), CircleShape)
            )
        }
        // Bottom ivory
        Box(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.45f).align(Alignment.BottomCenter)
                .background(IvoryWhite)
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            // Logo
            Box(
                modifier = Modifier.size(130.dp).scale(logoScale)
                    .background(PaperWhite.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(108.dp)
                        .background(PaperWhite, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = R.drawable.search,
                        contentDescription = "Logo",
                        modifier = Modifier.size(68.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Tagline on dark bg
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "CAMPUS", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    color = PaperWhite.copy(alpha = 0.5f), letterSpacing = 5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Lost & Found", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold,
                    color = PaperWhite, textAlign = TextAlign.Center
                )
            }

            // Stats row — sits on the boundary
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                shape = RoundedCornerShape(16.dp),
                color = PaperWhite,
                shadowElevation = 8.dp,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EntryStatItem("120+", "Items Found")
                    Box(modifier = Modifier.width(1.dp).height(36.dp).background(StrokeGray))
                    EntryStatItem("500+", "Students")
                    Box(modifier = Modifier.width(1.dp).height(36.dp).background(StrokeGray))
                    EntryStatItem("Fast", "Response")
                }
            }

            // CTA
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 44.dp)
            ) {
                Text(
                    "Reuniting students with their belongings",
                    fontSize = 13.sp, color = TextSecond, textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onGetStarted,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = PaperWhite),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Get Started", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("→", fontSize = 18.sp, fontWeight = FontWeight.Light)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text("Free for all campus students", fontSize = 12.sp, color = TextHint)
            }
        }
    }
}

@Composable
private fun EntryStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Charcoal)
        Text(label, fontSize = 11.sp, color = TextSecond)
    }
}
