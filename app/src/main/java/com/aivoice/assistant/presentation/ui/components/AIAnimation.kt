package com.aivoice.assistant.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.aivoice.assistant.domain.model.AnimationState
import com.aivoice.assistant.presentation.ui.theme.*
import kotlin.math.*

@Composable
fun AIAssistantAnimation(
    state: AnimationState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_anim")

    // Asosiy pulsatsiya
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Aylana aylanuvi
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AnimationState.LISTENING -> 1500
                    AnimationState.PROCESSING -> 800
                    AnimationState.SPEAKING -> 2000
                    else -> 4000
                },
                easing = LinearEasing
            )
        ),
        label = "rotation"
    )

    // To'lqin amplitude
    val waveAmplitude by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave"
    )

    // Renk animatsiyasi
    val colorAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val primaryColor = when (state) {
        AnimationState.IDLE -> PrimaryPurple
        AnimationState.LISTENING -> AccentGreen
        AnimationState.PROCESSING -> SecondaryBlue
        AnimationState.SPEAKING -> PrimaryPurpleLight
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f

            // Tashqi halqalar
            drawOuterRings(center, maxRadius, rotation, primaryColor, colorAlpha, state)

            // To'lqin halqalar (listening va speaking uchun)
            if (state == AnimationState.LISTENING || state == AnimationState.SPEAKING) {
                drawWaveRings(center, maxRadius * 0.75f, waveAmplitude, primaryColor)
            }

            // Markaziy doira
            drawCentralCircle(center, maxRadius * 0.38f * pulseScale, primaryColor, colorAlpha)

            // Processing orbs
            if (state == AnimationState.PROCESSING) {
                drawProcessingOrbs(center, maxRadius * 0.6f, rotation, SecondaryBlue)
            }
        }
    }
}

private fun DrawScope.drawOuterRings(
    center: Offset,
    radius: Float,
    rotation: Float,
    color: Color,
    alpha: Float,
    state: AnimationState
) {
    val ringCount = if (state == AnimationState.IDLE) 2 else 3
    for (i in 0 until ringCount) {
        val ringRadius = radius * (0.6f + i * 0.15f)
        val strokeWidth = (3f - i * 0.5f).coerceAtLeast(1f)
        val ringAlpha = alpha * (1f - i * 0.3f)

        drawCircle(
            color = color.copy(alpha = ringAlpha * 0.4f),
            radius = ringRadius,
            center = center,
            style = Stroke(width = strokeWidth)
        )
    }

    // Aylanuvchi arc
    val sweepAngle = if (state == AnimationState.PROCESSING) 270f else 120f
    drawArc(
        color = color.copy(alpha = 0.8f),
        startAngle = rotation,
        sweepAngle = sweepAngle,
        useCenter = false,
        style = Stroke(width = 3f),
        topLeft = Offset(center.x - radius * 0.72f, center.y - radius * 0.72f),
        size = androidx.compose.ui.geometry.Size(radius * 1.44f, radius * 1.44f)
    )
}

private fun DrawScope.drawWaveRings(
    center: Offset,
    baseRadius: Float,
    amplitude: Float,
    color: Color
) {
    val waveCount = 3
    for (i in 0 until waveCount) {
        val delay = i.toFloat() / waveCount
        val currentAmp = ((amplitude + delay) % 1f)
        val radius = baseRadius * (0.7f + currentAmp * 0.3f)
        val alpha = (1f - currentAmp) * 0.5f

        drawCircle(
            color = color.copy(alpha = alpha),
            radius = radius,
            center = center,
            style = Stroke(width = 2f)
        )
    }
}

private fun DrawScope.drawCentralCircle(
    center: Offset,
    radius: Float,
    color: Color,
    alpha: Float
) {
    // Gradient doira
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = alpha),
                color.copy(alpha = alpha * 0.5f),
                color.copy(alpha = 0f)
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )

    // Markaziy nuqta
    drawCircle(
        color = color.copy(alpha = 0.9f),
        radius = radius * 0.3f,
        center = center
    )
}

private fun DrawScope.drawProcessingOrbs(
    center: Offset,
    orbitRadius: Float,
    rotation: Float,
    color: Color
) {
    val orbCount = 3
    for (i in 0 until orbCount) {
        val angle = (rotation + i * 120f) * PI.toFloat() / 180f
        val orbCenter = Offset(
            center.x + orbitRadius * cos(angle),
            center.y + orbitRadius * sin(angle)
        )
        drawCircle(
            color = color.copy(alpha = 0.7f),
            radius = 8f,
            center = orbCenter
        )
    }
}
