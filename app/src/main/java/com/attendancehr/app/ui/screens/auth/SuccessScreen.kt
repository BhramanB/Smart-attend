package com.attendancehr.app.ui.screens.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.attendancehr.app.ui.components.PrimaryButton
import com.attendancehr.app.ui.theme.Blue600
import com.attendancehr.app.ui.theme.Navy950
import kotlinx.coroutines.delay

@Composable
fun SuccessScreen(
    message: String,
    onContinue: () -> Unit,
) {
    val start = remember { mutableStateOf(false) }
    val progress = animateFloatAsState(
        targetValue = if (start.value) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "checkProgress",
    )

    LaunchedEffect(Unit) {
        delay(120)
        start.value = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Navy950, Navy950.copy(alpha = 0.95f))))
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .shadow(
                        elevation = 18.dp,
                        shape = CircleShape,
                        ambientColor = Blue600.copy(alpha = 0.28f),
                        spotColor = Blue600.copy(alpha = 0.28f),
                    )
                    .background(Color.White.copy(alpha = 0.06f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedCheckmark(progress = progress.value)
            }

            Spacer(Modifier.size(4.dp))
            Text(text = "Success!", style = MaterialTheme.typography.headlineLarge)
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 18.dp),
            )

            Spacer(Modifier.size(10.dp))
            PrimaryButton(text = "Continue", onClick = onContinue)
        }
    }
}

@Composable
private fun AnimatedCheckmark(progress: Float) {
    Canvas(modifier = Modifier.size(64.dp)) {
        val stroke = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
        val w = size.width
        val h = size.height
        val ringColor = Blue600.copy(alpha = 0.95f)
        drawCircle(color = ringColor.copy(alpha = 0.18f), style = Stroke(width = 8.dp.toPx()))

        val p = Path().apply {
            moveTo(w * 0.22f, h * 0.52f)
            lineTo(w * 0.42f, h * 0.70f)
            lineTo(w * 0.78f, h * 0.34f)
        }

        // Draw partial path by splitting into 2 segments
        val a = Offset(w * 0.22f, h * 0.52f)
        val b = Offset(w * 0.42f, h * 0.70f)
        val c = Offset(w * 0.78f, h * 0.34f)

        val first = (progress * 2f).coerceIn(0f, 1f)
        val second = ((progress - 0.5f) * 2f).coerceIn(0f, 1f)

        val ab = Offset(
            x = a.x + (b.x - a.x) * first,
            y = a.y + (b.y - a.y) * first,
        )
        drawLine(color = Blue600, start = a, end = ab, strokeWidth = stroke.width, cap = stroke.cap)

        if (progress > 0.5f) {
            val bc = Offset(
                x = b.x + (c.x - b.x) * second,
                y = b.y + (c.y - b.y) * second,
            )
            drawLine(color = Blue600, start = b, end = bc, strokeWidth = stroke.width, cap = stroke.cap)
        }
    }
}

