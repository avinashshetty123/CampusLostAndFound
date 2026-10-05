package com.example.lostandfoundfrontend.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lostandfoundfrontend.R
import com.example.lostandfoundfrontend.data.LostFoundViewModel
import com.example.lostandfoundfrontend.ui.components.ServerWakingBanner
import com.example.lostandfoundfrontend.ui.components.pressScale
import com.example.lostandfoundfrontend.ui.components.shake
import com.example.lostandfoundfrontend.ui.components.staggeredEntrance
import com.example.lostandfoundfrontend.ui.theme.*

@Composable
fun AuthScreen(viewModel: LostFoundViewModel, onLoginSuccess: () -> Unit) {
    val authState by viewModel.authState.collectAsState()
    val serverWaking by viewModel.serverWaking.collectAsState()
    val focusManager = LocalFocusManager.current

    var isLoginMode by rememberSaveable { mutableStateOf(true) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var mobile by rememberSaveable { mutableStateOf("") }
    var studentClass by rememberSaveable { mutableStateOf("") }
    var department by rememberSaveable { mutableStateOf("") }
    var signUpPassword by remember { mutableStateOf("") }
    var signUpPasswordVisible by remember { mutableStateOf(false) }
    var agreeTerms by rememberSaveable { mutableStateOf(false) }

    // Navigate on success
    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) onLoginSuccess()
    }

    fun submitLogin() { focusManager.clearFocus(); viewModel.login(email, password) }
    fun submitRegister() { focusManager.clearFocus(); viewModel.register(name, email, mobile, studentClass, department, signUpPassword) }

    Column(modifier = Modifier.fillMaxSize().background(IvoryWhite).imePadding()) {
        // Gradient header
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Charcoal, Slate)))
                .padding(horizontal = 28.dp, vertical = 36.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(64.dp).staggeredEntrance(0)
                        .background(PaperWhite.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.size(52.dp).background(PaperWhite, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = R.drawable.search, contentDescription = "Logo",
                            modifier = Modifier.size(34.dp), contentScale = ContentScale.Fit
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Title/subtitle slide vertically when switching modes
                AnimatedContent(
                    targetState = isLoginMode,
                    transitionSpec = {
                        (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut())
                    },
                    label = "authTitle"
                ) { login ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (login) "Welcome Back" else "Create Account",
                            fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = PaperWhite
                        )
                        Text(
                            if (login) "Sign in to your campus account" else "Join the campus community",
                            fontSize = 13.sp, color = PaperWhite.copy(alpha = 0.65f)
                        )
                    }
                }
            }
        }

        ServerWakingBanner(serverWaking && authState.isLoading)

        // Tab switcher with a sliding indicator
        Surface(modifier = Modifier.fillMaxWidth(), color = PaperWhite, shadowElevation = 2.dp) {
            ModeSwitcher(isLoginMode) {
                isLoginMode = it
                viewModel.clearAuthError()
            }
        }

        // Error banner — slides in and shakes each time a new error arrives
        AnimatedVisibility(
            visible = authState.error != null,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)
                    .shake(authState.errorCount.takeIf { it > 0 }),
                shape = RoundedCornerShape(10.dp),
                color = LostRedBg
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = LostRed, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(authState.error ?: "", color = LostRed, fontSize = 13.sp)
                }
            }
        }

        // Form
        Column(
            modifier = Modifier.fillMaxSize().background(IvoryWhite)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            AnimatedContent(
                targetState = isLoginMode,
                transitionSpec = {
                    slideInHorizontally { if (targetState) -it else it } + fadeIn() togetherWith
                    slideOutHorizontally { if (targetState) it else -it } + fadeOut()
                },
                label = "formSwitch"
            ) { loginMode ->
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (loginMode) {
                        AuthField(email, { email = it }, "Email Address", Icons.Default.Email,
                            keyboardType = KeyboardType.Email, modifier = Modifier.staggeredEntrance(0))
                        AuthField(password, { password = it }, "Password", Icons.Default.Lock,
                            isPassword = true, passwordVisible = passwordVisible, onTogglePassword = { passwordVisible = !passwordVisible },
                            imeAction = ImeAction.Done, onDone = { submitLogin() }, modifier = Modifier.staggeredEntrance(1))
                        Spacer(Modifier.height(4.dp))
                        PrimaryButton(
                            text = "Log In", loading = authState.isLoading,
                            modifier = Modifier.staggeredEntrance(2), onClick = { submitLogin() }
                        )
                    } else {
                        AuthField(name, { name = it }, "Full Name", Icons.Default.Person, modifier = Modifier.staggeredEntrance(0))
                        AuthField(email, { email = it }, "Email Address", Icons.Default.Email,
                            keyboardType = KeyboardType.Email, modifier = Modifier.staggeredEntrance(1))
                        AuthField(mobile, { mobile = it }, "Mobile Number", Icons.Default.Phone,
                            keyboardType = KeyboardType.Phone, modifier = Modifier.staggeredEntrance(2))
                        AuthField(studentClass, { studentClass = it }, "Class (e.g. B.Tech 3rd Year)", Icons.Default.School,
                            modifier = Modifier.staggeredEntrance(3))
                        AuthField(department, { department = it }, "Department", Icons.Default.Business,
                            modifier = Modifier.staggeredEntrance(4))
                        AuthField(signUpPassword, { signUpPassword = it }, "Password (min 6 characters)", Icons.Default.Lock,
                            isPassword = true, passwordVisible = signUpPasswordVisible,
                            onTogglePassword = { signUpPasswordVisible = !signUpPasswordVisible },
                            imeAction = ImeAction.Done, onDone = { if (agreeTerms) submitRegister() },
                            modifier = Modifier.staggeredEntrance(5))
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.staggeredEntrance(6)) {
                            Checkbox(checked = agreeTerms, onCheckedChange = { agreeTerms = it },
                                colors = CheckboxDefaults.colors(checkedColor = Charcoal))
                            Text("I agree to the Terms & Conditions", fontSize = 13.sp, color = TextPrimary)
                        }
                        PrimaryButton(
                            text = "Create Account", loading = authState.isLoading, enabled = agreeTerms,
                            modifier = Modifier.staggeredEntrance(7), onClick = { submitRegister() }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ModeSwitcher(isLoginMode: Boolean, onChange: (Boolean) -> Unit) {
    var width by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val half = with(density) { (width / 2).toDp() }
    val indicatorOffset by animateDpAsState(
        targetValue = if (isLoginMode) 0.dp else half,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow),
        label = "tabIndicator"
    )
    Box(
        modifier = Modifier.fillMaxWidth().padding(12.dp)
            .clip(RoundedCornerShape(12.dp)).background(SurfaceGray)
            .onSizeChanged { width = it.width }
    ) {
        Box(
            Modifier.offset(x = indicatorOffset).width(half).height(44.dp)
                .padding(3.dp).clip(RoundedCornerShape(10.dp)).background(Charcoal)
        )
        Row(Modifier.fillMaxWidth().height(44.dp)) {
            listOf("Login", "Sign Up").forEachIndexed { index, label ->
                val selected = (index == 0) == isLoginMode
                val color by animateColorAsState(if (selected) PaperWhite else TextSecond, label = "tabText")
                TextButton(
                    onClick = { onChange(index == 0) },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(label, color = color, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
internal fun PrimaryButton(
    text: String,
    loading: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        interactionSource = interaction,
        modifier = modifier.fillMaxWidth().height(52.dp).pressScale(interaction),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Charcoal, contentColor = PaperWhite,
            disabledContainerColor = Charcoal.copy(alpha = if (loading) 0.85f else 0.35f), disabledContentColor = PaperWhite
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp)
    ) {
        // Label and spinner cross-fade instead of popping
        AnimatedContent(targetState = loading, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "btnContent") { isLoading ->
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PaperWhite, strokeWidth = 2.dp)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AuthField(
    value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false, passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onDone: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = TextSecond, modifier = Modifier.size(18.dp)) },
        trailingIcon = if (isPassword) ({
            IconButton(onClick = { onTogglePassword?.invoke() }) {
                Crossfade(passwordVisible, label = "eye") { visible ->
                    Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (visible) "Hide password" else "Show password",
                        tint = TextSecond, modifier = Modifier.size(18.dp))
                }
            }
        }) else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Charcoal, unfocusedBorderColor = StrokeGray,
            focusedContainerColor = PaperWhite, unfocusedContainerColor = PaperWhite,
            focusedLabelColor = Charcoal, unfocusedLabelColor = TextSecond
        ),
        singleLine = true
    )
}
