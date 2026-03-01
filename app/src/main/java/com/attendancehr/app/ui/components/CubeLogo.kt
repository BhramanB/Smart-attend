package com.attendancehr.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.attendancehr.app.ui.theme.Blue300
import com.attendancehr.app.ui.theme.Blue600

@Composable
fun CubeLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val cx = w / 2f
        val top = h * 0.12f
        val mid = h * 0.46f
        val bot = h * 0.86f
        val dx = w * 0.28f

        val topFace = Path().apply {
            moveTo(cx, top)
            lineTo(cx + dx, mid)
            lineTo(cx, mid + dx * 0.15f)
            lineTo(cx - dx, mid)
            close()
        }

        val leftFace = Path().apply {
            moveTo(cx - dx, mid)
            lineTo(cx, mid + dx * 0.15f)
            lineTo(cx, bot)
            lineTo(cx - dx, bot - dx * 0.2f)
            close()
        }

        val rightFace = Path().apply {
            moveTo(cx + dx, mid)
            lineTo(cx, mid + dx * 0.15f)
            lineTo(cx, bot)
            lineTo(cx + dx, bot - dx * 0.2f)
            close()
        }

        drawPath(topFace, color = Blue300.copy(alpha = 0.9f))
        drawPath(leftFace, color = Blue600.copy(alpha = 0.9f))
        drawPath(rightFace, color = Blue600.copy(alpha = 0.7f))

        val stroke = Stroke(width = 2.dp.toPx())
        drawPath(topFace, color = Color.White.copy(alpha = 0.22f), style = stroke)
        drawPath(leftFace, color = Color.White.copy(alpha = 0.18f), style = stroke)
        drawPath(rightFace, color = Color.White.copy(alpha = 0.18f), style = stroke)

        // subtle inner glow
        drawRect(
            color = Blue600.copy(alpha = 0.10f),
            topLeft = Offset(0f, 0f),
            size = Size(w, h),
        )
    }
}

