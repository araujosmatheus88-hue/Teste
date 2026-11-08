package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier
) {
    // Pulse and entrance animation
    val infiniteTransition = rememberInfiniteTransition(label = "SplashNeonPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    var startAnimation by remember { mutableStateOf(false) }
    val entryAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(700, easing = EaseOutCubic),
        label = "EntryAlpha"
    )
    val entryScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "EntryScale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Subtle ambient neon halo behind mascot
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(pulseScale)
                .alpha(glowAlpha)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NeonPink.copy(alpha = 0.30f),
                            ElectricPurple.copy(alpha = 0.15f),
                            AccentYellow.copy(alpha = 0.05f),
                            DeepBlack.copy(alpha = 0f)
                        )
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(entryAlpha)
                .scale(entryScale)
                .padding(24.dp)
        ) {
            // Mascot Container with Neon Border
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(CircleShape)
                    .background(DeepSurfaceVariant)
                    .border(2.5.dp, Brush.sweepGradient(listOf(NeonPink, ElectricPurple, AccentYellow, NeonPink)), CircleShape)
                    .shadow(16.dp, CircleShape, spotColor = NeonPink)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_mascot_splash_1794160011622),
                    contentDescription = "Drama Box Mascote Robô",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // DRAMA BOX Official Typography
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "DRAMA",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        fontSize = 36.sp
                    ),
                    color = TextWhite
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BOX",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        fontSize = 36.sp
                    ),
                    color = NeonPink
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "STREAMING DE MININOVELAS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Lavender
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Discreet Neon Loading Indicator
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = NeonPink,
                trackColor = ElectricPurple.copy(alpha = 0.25f),
                strokeWidth = 2.5.dp
            )
        }
    }
}
