package com.example.lostandfoundfrontend.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lostandfoundfrontend.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Fades and slides content up the first time it appears, delayed by [index] so
 * lists cascade in. Uses rememberSaveable so items scrolled back into view
 * (in a keyed LazyColumn) don't replay the animation.
 */
fun Modifier.staggeredEntrance(index: Int, baseDelayMs: Int = 55, offsetY: Dp = 28.dp): Modifier = composed {
    var played by rememberSaveable { mutableStateOf(false) }
    val progress = remember { Animatable(if (played) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!played) {
            delay(index.coerceIn(0, 8) * baseDelayMs.toLong())
            progress.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
            played = true
        }
    }
    val offsetPx = with(LocalDensity.current) { offsetY.toPx() }
    graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * offsetPx
    }
}

/** Springy shrink while pressed — pass the same interactionSource given to the clickable/Button. */
fun Modifier.pressScale(interactionSource: InteractionSource, pressedScale: Float = 0.95f): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMedium),
        label = "pressScale"
    )
    graphicsLayer { scaleX = scale; scaleY = scale }
}

/** Clickable with a bouncy press animation. */
fun Modifier.bounceClick(enabled: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    this
        .pressScale(interaction)
        .clickable(interactionSource = interaction, indication = androidx.compose.foundation.LocalIndication.current, enabled = enabled, onClick = onClick)
}

/** Horizontal shake, replayed every time [trigger] changes to a new non-null value. */
fun Modifier.shake(trigger: Any?): Modifier = composed {
    val offset = remember { Animatable(0f) }
    LaunchedEffect(trigger) {
        if (trigger != null) {
            for (x in listOf(18f, -16f, 12f, -8f, 4f, 0f)) offset.animateTo(x, tween(45))
        }
    }
    graphicsLayer { translationX = offset.value }
}

/** Moving highlight used by loading skeletons. */
fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "shimmerProgress"
    )
    drawWithContent {
        val w = size.width
        val start = -w + progress * 2f * w
        drawRect(
            Brush.linearGradient(
                listOf(SurfaceGray, StrokeGray, SurfaceGray),
                start = Offset(start, 0f),
                end = Offset(start + w, size.height)
            )
        )
    }
}

/** Counts up/down to [target] whenever it changes. */
@Composable
fun animatedCount(target: Long): String {
    val value by animateIntAsState(
        targetValue = target.coerceAtMost(Int.MAX_VALUE.toLong()).toInt(),
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "count"
    )
    return value.toString()
}

/** Placeholder card shown while the first page of items loads. */
@Composable
fun ShimmerItemCard(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = PaperWhite, shadowElevation = 2.dp) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(54.dp).clip(RoundedCornerShape(14.dp)).shimmer())
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Box(Modifier.fillMaxWidth(0.6f).height(14.dp).clip(RoundedCornerShape(6.dp)).shimmer())
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth(0.4f).height(10.dp).clip(RoundedCornerShape(6.dp)).shimmer())
                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.fillMaxWidth(0.3f).height(10.dp).clip(RoundedCornerShape(6.dp)).shimmer())
                }
            }
            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(6.dp)).shimmer())
        }
    }
}

/** Thin banner shown while the (free-tier) server is cold-starting. */
@Composable
fun ServerWakingBanner(visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(visible = visible, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut(), modifier = modifier) {
        val transition = rememberInfiniteTransition(label = "waking")
        val pulse by transition.animateFloat(
            initialValue = 0.35f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(700), androidx.compose.animation.core.RepeatMode.Reverse),
            label = "pulse"
        )
        Row(
            modifier = Modifier.fillMaxWidth().background(AccentGold.copy(alpha = 0.15f)).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(8.dp).graphicsLayer { alpha = pulse }.background(AccentGold, CircleShape))
            Spacer(Modifier.width(10.dp))
            Text("Waking up the server — this can take up to a minute…", fontSize = 12.sp, color = TextPrimary)
        }
    }
}

/**
 * Full-screen celebration: a circle springs in, a checkmark draws itself, then the
 * text fades in. [onFinished] runs once the sequence completes.
 */
@Composable
fun SuccessOverlay(visible: Boolean, title: String, subtitle: String, onFinished: () -> Unit) {
    AnimatedVisibility(visible = visible, enter = fadeIn(tween(200)), exit = fadeOut(tween(200))) {
        val circle = remember { Animatable(0f) }
        val check = remember { Animatable(0f) }
        val text = remember { Animatable(0f) }
        val currentOnFinished by rememberUpdatedState(onFinished)
        LaunchedEffect(Unit) {
            launch { circle.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow)) }
            delay(220)
            check.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
            text.animateTo(1f, tween(260))
            delay(900)
            currentOnFinished()
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Charcoal.copy(alpha = 0.94f))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(110.dp)
                        .graphicsLayer { scaleX = circle.value; scaleY = circle.value }
                        .background(FoundGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(Modifier.size(56.dp)) {
                        val path = Path().apply {
                            moveTo(size.width * 0.12f, size.height * 0.52f)
                            lineTo(size.width * 0.40f, size.height * 0.78f)
                            lineTo(size.width * 0.90f, size.height * 0.22f)
                        }
                        val measure = PathMeasure().apply { setPath(path, false) }
                        val segment = Path()
                        measure.getSegment(0f, measure.length * check.value, segment, true)
                        drawPath(segment, PaperWhite, style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                    }
                }
                Spacer(Modifier.height(24.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer { alpha = text.value; translationY = (1f - text.value) * 30f }
                ) {
                    Text(title, color = PaperWhite, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Text(subtitle, color = PaperWhite.copy(alpha = 0.7f), fontSize = 14.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 40.dp))
                }
            }
        }
    }
}
