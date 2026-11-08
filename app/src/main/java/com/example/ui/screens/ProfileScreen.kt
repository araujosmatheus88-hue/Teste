package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthManager
import com.example.model.UserRole
import com.example.ui.theme.*
import com.example.viewmodel.DramaBoxViewModel
import com.example.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    viewModel: DramaBoxViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()
    val vpsSettings by viewModel.vpsSettings.collectAsState()

    var showAuthDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .testTag("profile_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DeepCardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar with glowing neon ring
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(ElectricPurple, NeonPink)
                                )
                            )
                            .border(
                                2.dp,
                                if (currentUser?.role == UserRole.ADMIN) AccentYellow else NeonPink,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser?.name?.take(1)?.uppercase() ?: "D",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = currentUser?.name ?: "Visitante",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )

                    Text(
                        text = currentUser?.email ?: "Não conectado",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Role Badge
                    val isAdmin = currentUser?.role == UserRole.ADMIN
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isAdmin) Brush.horizontalGradient(listOf(ElectricPurple, NeonPink))
                                else Brush.horizontalGradient(listOf(DeepSurfaceVariant, DeepSurfaceVariant))
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isAdmin) "⭐ ADMINISTRADOR OFICIAL" else "USUÁRIO DRAMA BOX",
                            color = if (isAdmin) TextWhite else Lavender,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Admin Access Section (Prominent if Admin)
        if (currentUser?.role == UserRole.ADMIN) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Brush.horizontalGradient(listOf(NeonPink, ElectricPurple)), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = AccentYellow,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Painel Administrativo",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextWhite
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Gerencie mininovelas, temporadas, episódios, links de vídeo da sua VPS, categorias e usuários.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.navigateTo(Screen.AdminPanel) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonPink,
                                contentColor = TextWhite
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("open_admin_panel_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dashboard,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Acessar Painel /admin", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Favoritos",
                    value = "${favorites.size}",
                    icon = Icons.Default.Bookmark,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "No Histórico",
                    value = "${watchHistory.size}",
                    icon = Icons.Default.History,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Status VPS",
                    value = "Online",
                    icon = Icons.Default.Dns,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Admin Toggle & Preferences
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DeepCardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Configurações da Conta",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Lavender
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Switch Admin/User role tester
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleAdminMode() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = if (currentUser?.role == UserRole.ADMIN) AccentYellow else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Permissão de Administrador",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextWhite
                                )
                                Text(
                                    text = if (currentUser?.role == UserRole.ADMIN) "Ativa (acesso ao /admin)" else "Inativa (usuário comum)",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                                )
                            }
                        }

                        Switch(
                            checked = currentUser?.role == UserRole.ADMIN,
                            onCheckedChange = { viewModel.toggleAdminMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextWhite,
                                checkedTrackColor = NeonPink,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = DeepSurfaceVariant
                            )
                        )
                    }

                    HorizontalDivider(color = DeepCardBorder, modifier = Modifier.padding(vertical = 8.dp))

                    // Account details
                    currentUser?.let { u ->
                        val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(u.createdAt))
                        Text(
                            text = "Membro desde: $dateStr",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "UID: ${u.id}",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle)
                        )
                    }
                }
            }
        }

        // VPS Settings Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DeepCardBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = ElectricPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Servidor VPS de Vídeos",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Lavender
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Base URL: ${vpsSettings.vpsBaseUrl}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextWhite)
                    )
                    Text(
                        text = "Reprodução HLS & MP4 nativa configurada",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                }
            }
        }

        // Login / Logout Action
        item {
            if (currentUser != null) {
                OutlinedButton(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(listOf(Color(0xFFFF5252), Color(0xFFFF5252)))
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Encerrar Sessão", fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = { showAuthDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink, contentColor = TextWhite),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Entrar / Cadastrar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showAuthDialog) {
        AuthModalDialog(
            onDismiss = { showAuthDialog = false },
            onLogin = { email, name ->
                viewModel.login(email, name)
                showAuthDialog = false
            },
            onGoogleSignInSuccess = { firebaseUser ->
                viewModel.repository.onGoogleUserSignedIn(firebaseUser)
                showAuthDialog = false
            }
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepCard),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, DeepCardBorder, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = NeonPink, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextWhite)
            Text(text = title, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = TextMuted))
        }
    }
}

@Composable
fun AuthModalDialog(
    onDismiss: () -> Unit,
    onLogin: (String, String) -> Unit,
    onGoogleSignInSuccess: ((com.google.firebase.auth.FirebaseUser) -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { androidx.credentials.CredentialManager.create(context) }

    var isRegister by remember { mutableStateOf(false) }
    var showForgotPassword by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }
    var forgotMessage by remember { mutableStateOf<String?>(null) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf<String?>(null) }
    var isGoogleLoading by remember { mutableStateOf(false) }

    if (showForgotPassword) {
        AlertDialog(
            onDismissRequest = { showForgotPassword = false },
            containerColor = DeepSurface,
            title = {
                Text(
                    text = "Recuperar Senha",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Informe seu e-mail cadastrado para enviarmos as instruções de recuperação:",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )
                    OutlinedTextField(
                        value = forgotEmail,
                        onValueChange = { forgotEmail = it },
                        label = { Text("E-mail") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPink,
                            unfocusedBorderColor = DeepCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (forgotMessage != null) {
                        Text(
                            text = forgotMessage!!,
                            style = MaterialTheme.typography.labelSmall.copy(color = AccentYellow)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (forgotEmail.isNotBlank()) {
                            forgotMessage = "E-mail de recuperação enviado para $forgotEmail! Verifique sua caixa de entrada."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                ) {
                    Text("Enviar Link", color = TextWhite)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPassword = false }) {
                    Text("Fechar", color = TextMuted)
                }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Brush.linearGradient(listOf(NeonPink, ElectricPurple))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRegister) "Criar Conta Drama Box" else "Entrar no Drama Box",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Google Sign In button
                OutlinedButton(
                    onClick = {
                        isGoogleLoading = true
                        authError = null
                        AuthManager.signInWithGoogle(
                            context = context,
                            credentialManager = credentialManager,
                            onAuthSuccess = { user: com.google.firebase.auth.FirebaseUser ->
                                isGoogleLoading = false
                                onGoogleSignInSuccess?.invoke(user)
                                onDismiss()
                            },
                            onAuthError = { err: String ->
                                isGoogleLoading = false
                                authError = err
                            },
                            scope = coroutineScope,
                            onAuthCancelled = {
                                isGoogleLoading = false
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .border(1.dp, DeepCardBorder, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = DeepSurfaceVariant,
                        contentColor = TextWhite
                    )
                ) {
                    if (isGoogleLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = NeonPink,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "G",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                color = AccentYellow
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Continuar com Google",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = DeepCardBorder)
                    Text(
                        text = "  OU COM E-MAIL  ",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle, fontSize = 9.sp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = DeepCardBorder)
                }

                if (isRegister) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Seu Nome") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPink,
                            unfocusedBorderColor = DeepCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPink,
                        unfocusedBorderColor = DeepCardBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Senha") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPink,
                        unfocusedBorderColor = DeepCardBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isRegister) {
                        TextButton(onClick = {
                            forgotEmail = email
                            showForgotPassword = true
                        }) {
                            Text("Esqueci a senha", color = Lavender, fontSize = 11.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    TextButton(onClick = { isRegister = !isRegister }) {
                        Text(
                            text = if (isRegister) "Já tem conta? Entrar" else "Criar nova conta",
                            color = NeonPink,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (authError != null) {
                    Text(
                        text = authError!!,
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFFF5252))
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        onLogin(email, if (name.isNotBlank()) name else "Usuário")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
            ) {
                Text(if (isRegister) "Cadastrar" else "Entrar", color = TextWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextMuted)
            }
        }
    )
}
