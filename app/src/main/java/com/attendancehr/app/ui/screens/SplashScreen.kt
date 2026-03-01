package com.attendancehr.app.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.attendancehr.app.ui.components.CubeLogo
import com.attendancehr.app.ui.theme.Blue600
import com.attendancehr.app.ui.theme.Navy950
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val started = remember { mutableStateOf(false) }
    val alpha = animateFloatAsState(
        targetValue = if (started.value) 1f else 0f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "splashAlpha",
    )
    val scale = animateFloatAsState(
        targetValue = if (started.value) 1f else 0.92f,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "splashScale",
    )

    LaunchedEffect(Unit) {
        started.value = true
        delay(900)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Blue600, Navy950),
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        CubeLogo(
            modifier = Modifier
                .size(78.dp)
                .alpha(alpha.value)
                .scale(scale.value),
        )
    }
}

