package com.aivoice.assistant.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.aivoice.assistant.presentation.ui.theme.*

@Composable
fun MicrophoneButton(
    isListening: Boolean,
    isProcessing: Boolean,
    isSpeaking: Boolean,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse"
    )

    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = if (isListening) 1f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_alpha"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isListening -> AccentGreen.copy(alpha = 0.2f)
            isProcessing -> SecondaryBlue.copy(alpha = 0.2f)
            isSpeaking -> PrimaryPurple.copy(alpha = 0.2f)
            else -> DarkCard
        },
        animationSpec = tween(300),
        label = "bg_color"
    )

    val iconColor by animateColorAsState(
        targetValue = when {
            isListening -> AccentGreen
            isProcessing -> SecondaryBlue
            isSpeaking -> PrimaryPurpleLight
            else -> TextPrimary
        },
        animationSpec = tween(300),
        label = "icon_color"
    )

    val borderColor = when {
        isListening -> AccentGreen
        isProcessing -> SecondaryBlue
        isSpeaking -> PrimaryPurple
        else -> DarkBorder
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .scale(if (isListening) pulseScale else 1f)
            .size(80.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(
                width = 2.dp,
                color = borderColor.copy(alpha = if (isListening) borderAlpha else 0.6f),
                shape = CircleShape
            )
            .pointerInput(isListening, isProcessing, isSpeaking) {
                detectTapGestures(
                    onTap = {
                        if (isListening) onStopListening() else onStartListening()
                    }
                )
            }
    ) {
        val icon = when {
            isListening -> Icons.Default.Stop
            else -> Icons.Default.Mic
        }
        Icon(
            imageVector = icon,
            contentDescription = if (isListening) "Stop" else "Start",
            tint = iconColor,
            modifier = Modifier.size(32.dp)
        )
    }
}
