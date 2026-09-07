package com.example.lostandfoundfrontend.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.lostandfoundfrontend.R
import com.example.lostandfoundfrontend.data.AuthUiState
import com.example.lostandfoundfrontend.data.LostFoundViewModel
import com.example.lostandfoundfrontend.ui.theme.*

@Composable
fun AuthScreen(viewModel: LostFoundViewModel, onLoginSuccess: () -> Unit) {
    val authState by viewModel.authState.collectAsState()

    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var studentClass by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var signUpPassword by remember { mutableStateOf("") }
    var signUpPasswordVisible by remember { mutableStateOf(false) }
    var agreeTerms by remember { mutableStateOf(false) }

    // Navigate on success
    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) onLoginSuccess()
    }

    Column(modifier = Modifier.fillMaxSize().background(IvoryWhite)) {
        // Gradient header
        Box(
            modifier = Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Charcoal, Slate)))
                .padding(horizontal = 28.dp, vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.size(64.dp)
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
                Text(
                    if (isLoginMode) "Welcome Back" else "Create Account",
                    fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = PaperWhite
                )
                Text(
                    if (isLoginMode) "Sign in to your campus account" else "Join the campus community",
                    fontSize = 13.sp, color = PaperWhite.copy(alpha = 0.65f)
                )
            }
        }

        // Tab switcher
        Surface(modifier = Modifier.fillMaxWidth(), color = PaperWhite, shadowElevation = 2.dp) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Login", "Sign Up").forEachIndexed { index, label ->
                    val selected = (index == 0) == isLoginMode
                    Button(
                        onClick = { isLoginMode = index == 0 },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) Charcoal else SurfaceGray,
                            contentColor = if (selected) PaperWhite else TextSecond
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
                    }
                }
            }
        }

        // Error banner
        if (authState.error != null) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                shape = RoundedCornerShape(10.dp),
                color = LostRedBg
            ) {
                Text(
                    authState.error ?: "",
                    color = LostRed, fontSize = 13.sp,
                    modifier = Modifier.padding(12.dp)
                )
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
                        AuthField(value = email, onValueChange = { email = it }, label = "Email Address", icon = Icons.Default.Email)
                        AuthField(value = password, onValueChange = { password = it }, label = "Password", icon = Icons.Default.Lock,
                            isPassword = true, passwordVisible = passwordVisible, onTogglePassword = { passwordVisible = !passwordVisible })
                        Text("Forgot password?", color = Charcoal, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                        Button(
                            onClick = {
                                if (email.isNotBlank() && password.isNotBlank()) {
                                    viewModel.login(email, password) {}
                                }
                            },
                            enabled = !authState.isLoading,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = PaperWhite),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            if (authState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PaperWhite, strokeWidth = 2.dp)
                            } else {
                                Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        AuthField(value = name, onValueChange = { name = it }, label = "Full Name", icon = Icons.Default.Person)
                        AuthField(value = email, onValueChange = { email = it }, label = "Email Address", icon = Icons.Default.Email)
                        AuthField(value = mobile, onValueChange = { mobile = it }, label = "Mobile Number", icon = Icons.Default.Phone)
                        AuthField(value = studentClass, onValueChange = { studentClass = it }, label = "Class (e.g. B.Tech 3rd Year)", icon = Icons.Default.School)
                        AuthField(value = department, onValueChange = { department = it }, label = "Department", icon = Icons.Default.Business)
                        AuthField(value = signUpPassword, onValueChange = { signUpPassword = it }, label = "Password", icon = Icons.Default.Lock,
                            isPassword = true, passwordVisible = signUpPasswordVisible, onTogglePassword = { signUpPasswordVisible = !signUpPasswordVisible })
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = agreeTerms, onCheckedChange = { agreeTerms = it },
                                colors = CheckboxDefaults.colors(checkedColor = Charcoal))
                            Text("I agree to the Terms & Conditions", fontSize = 13.sp, color = TextPrimary)
                        }
                        Button(
                            onClick = {
                                if (name.isNotBlank() && email.isNotBlank() && signUpPassword.isNotBlank() && agreeTerms) {
                                    viewModel.register(name, email, mobile, studentClass, department, signUpPassword) {}
                                }
                            },
                            enabled = !authState.isLoading && agreeTerms,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Charcoal, contentColor = PaperWhite),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            if (authState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PaperWhite, strokeWidth = 2.dp)
                            } else {
                                Text("Create Account", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun AuthField(
    value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector,
    isPassword: Boolean = false, passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null, error: String? = null
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = TextSecond, modifier = Modifier.size(18.dp)) },
        trailingIcon = if (isPassword) ({
            IconButton(onClick = { onTogglePassword?.invoke() }) {
                Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null, tint = TextSecond, modifier = Modifier.size(18.dp))
            }
        }) else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        isError = error != null,
        supportingText = { if (error != null) Text(error, color = LostRed, fontSize = 11.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Charcoal, unfocusedBorderColor = StrokeGray,
            focusedContainerColor = PaperWhite, unfocusedContainerColor = PaperWhite,
            focusedLabelColor = Charcoal, unfocusedLabelColor = TextSecond
        ),
        singleLine = true
    )
}
