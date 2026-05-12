package com.example.janaushadhifinder

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {

    val scale     = remember { Animatable(0f) }
    val alpha     = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val tagAlpha  = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Logo pops in
        scale.animateTo(1f, animationSpec = tween(600))
        // App name fades in
        alpha.animateTo(1f, animationSpec = tween(500))
        delay(200)
        // Tagline fades in
        textAlpha.animateTo(1f, animationSpec = tween(500))
        delay(200)
        tagAlpha.animateTo(1f, animationSpec = tween(400))
        // Wait then proceed
        delay(1200)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF00897B)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App logo circle
            Box(
                modifier = Modifier
                    .scale(scale.value)
                    .size(110.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💊",
                    fontSize = 52.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App name
            Text(
                text = "Jan-Aushadhi",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = alpha.value),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Finder",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB2DFDB).copy(alpha = alpha.value),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tagline
            Text(
                text = "Affordable medicines for everyone",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = textAlpha.value),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom badge
            Box(
                modifier = Modifier
                    .alpha(tagAlpha.value)
                    .background(
                        Color.White.copy(alpha = 0.2f),
                        androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "PM Jan-Aushadhi Yojana",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottom version text
        Text(
            text = "v1.0",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}