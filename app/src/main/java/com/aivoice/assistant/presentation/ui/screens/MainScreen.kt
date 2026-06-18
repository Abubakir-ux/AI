package com.aivoice.assistant.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivoice.assistant.domain.model.AnimationState
import com.aivoice.assistant.domain.model.AppLanguage
import com.aivoice.assistant.domain.model.AssistantState
import com.aivoice.assistant.presentation.ui.components.*
import com.aivoice.assistant.presentation.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: AssistantState,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onClearChat: () -> Unit,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSettings by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val keyboard = LocalSoftwareKeyboardController.current

    // Yangi xabar kelganda scroll
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Bar
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    when (uiState.animationState) {
                                        AnimationState.LISTENING -> AccentGreen
                                        AnimationState.PROCESSING -> SecondaryBlue
                                        AnimationState.SPEAKING -> PrimaryPurpleLight
                                        else -> TextMuted
                                    }
                                )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "AI Assistant",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = when (uiState.animationState) {
                                AnimationState.LISTENING -> "tinglayapman..."
                                AnimationState.PROCESSING -> "o'ylamoqdaman..."
                                AnimationState.SPEAKING -> "gapiryapman..."
                                else -> ""
                            },
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                ),
                actions = {
                    // Til tugmasi
                    TextButton(onClick = {
                        val next = when (uiState.currentLanguage) {
                            AppLanguage.UZBEK -> AppLanguage.RUSSIAN
                            AppLanguage.RUSSIAN -> AppLanguage.ENGLISH
                            AppLanguage.ENGLISH -> AppLanguage.UZBEK
                        }
                        onLanguageChange(next)
                    }) {
                        Text(
                            text = when (uiState.currentLanguage) {
                                AppLanguage.UZBEK -> "🇺🇿 UZ"
                                AppLanguage.RUSSIAN -> "🇷🇺 RU"
                                AppLanguage.ENGLISH -> "🇬🇧 EN"
                            },
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = { showSettings = !showSettings }) {
                        Icon(Icons.Default.Settings, "Settings", tint = TextSecondary)
                    }
                }
            )

            // Settings panel
            AnimatedVisibility(
                visible = showSettings,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                SettingsPanel(
                    currentLanguage = uiState.currentLanguage,
                    onLanguageChange = onLanguageChange,
                    onClearChat = {
                        onClearChat()
                        showSettings = false
                    }
                )
            }

            // Error
            uiState.error?.let { error ->
                AnimatedVisibility(visible = true) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ErrorRed.copy(alpha = 0.15f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "⚠️ $error",
                            color = ErrorRed,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // AI Animation
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                DarkSurface,
                                DarkBackground
                            )
                        )
                    )
            ) {
                AIAssistantAnimation(
                    state = uiState.animationState,
                    modifier = Modifier.size(220.dp)
                )

                // Status matn
                Text(
                    text = when (uiState.animationState) {
                        AnimationState.LISTENING -> "🎤 Gapiring..."
                        AnimationState.PROCESSING -> "🧠 Qayta ishlamoqda..."
                        AnimationState.SPEAKING -> "🔊 Javob beramoqda..."
                        else -> "Mikrofon tugmasini bosing"
                    },
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                )
            }

            // Chat tarixi
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (uiState.messages.isEmpty()) {
                    EmptyChat(modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.messages, key = { it.id }) { message ->
                            ChatMessageItem(message = message)
                        }
                    }
                }
            }

            // Bottom: Mikrofon + Matn kiritish
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, DarkSurface)
                        )
                    )
                    .padding(16.dp)
            ) {
                // Matn kiritish
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = {
                            Text("Yozing yoki gapiring...", color = TextMuted, fontSize = 14.sp)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = PrimaryPurple,
                            focusedContainerColor = DarkCard,
                            unfocusedContainerColor = DarkCard
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (textInput.isNotBlank()) {
                                onSendMessage(textInput)
                                textInput = ""
                                keyboard?.hide()
                            }
                        })
                    )
                    Spacer(Modifier.width(8.dp))
                    if (textInput.isNotBlank()) {
                        IconButton(
                            onClick = {
                                onSendMessage(textInput)
                                textInput = ""
                                keyboard?.hide()
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(50))
                                .background(PrimaryPurple)
                        ) {
                            Icon(Icons.Default.Send, "Send", tint = Color.White)
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Mikrofon tugmasi
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MicrophoneButton(
                        isListening = uiState.isListening,
                        isProcessing = uiState.isProcessing,
                        isSpeaking = uiState.isSpeaking,
                        onStartListening = onStartListening,
                        onStopListening = onStopListening
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyChat(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(32.dp)
    ) {
        Text(text = "👋", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Assalomu alaykum!\nMen sizning AI yordamchingizman",
            color = TextSecondary,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(20.dp))

        val commands = listOf(
            "💡 \"Fonarni yoq\"",
            "📷 \"Kamerani och\"",
            "📱 \"Telegramni och\"",
            "🕐 \"Soat nechi bo'ldi?\"",
            "🌤 \"Ob-havo qanday?\"",
            "📞 \"Akamga qo'ng'iroq qil\""
        )
        commands.forEach { cmd ->
            Text(
                text = cmd,
                color = TextMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 3.dp)
            )
        }
    }
}

@Composable
private fun SettingsPanel(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    onClearChat: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkCard)
            .padding(16.dp)
    ) {
        Text("Sozlamalar", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))

        Text("Til tanlash:", color = TextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppLanguage.entries.forEach { lang ->
                FilterChip(
                    selected = currentLanguage == lang,
                    onClick = { onLanguageChange(lang) },
                    label = {
                        Text(
                            text = when (lang) {
                                AppLanguage.UZBEK -> "🇺🇿 O'zbek"
                                AppLanguage.RUSSIAN -> "🇷🇺 Русский"
                                AppLanguage.ENGLISH -> "🇬🇧 English"
                            },
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryPurple.copy(alpha = 0.3f),
                        selectedLabelColor = PrimaryPurpleLight
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onClearChat,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Chatni tozalash", fontSize = 13.sp)
        }
    }
}
