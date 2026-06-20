package com.aivoice.assistant.domain.model

// Chat xabari modeli
data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val language: String = "uz"
)

// Buyruq turlari
sealed class VoiceCommand {
    object Greeting : VoiceCommand()
    object FlashlightOn : VoiceCommand()
    object FlashlightOff : VoiceCommand()
    object OpenCamera : VoiceCommand()
    object OpenTelegram : VoiceCommand()
    object OpenYouTube : VoiceCommand()
    object GetCurrentTime : VoiceCommand()
    object GetWeather : VoiceCommand()
    data class MakeCall(val contactName: String) : VoiceCommand()
    data class SendSms(val contactName: String, val message: String) : VoiceCommand()
    data class OpenApp(val packageName: String, val appName: String) : VoiceCommand()
    data class AskAI(val question: String) : VoiceCommand()
    object Unknown : VoiceCommand()
}

// Buyruq natijasi
sealed class CommandResult {
    data class Success(val message: String) : CommandResult()
    data class Error(val message: String) : CommandResult()
    data class Pending(val message: String) : CommandResult()
}

// Til sozlamalari
enum class AppLanguage(val code: String, val displayName: String, val locale: String) {
    UZBEK("uz", "O'zbek", "uz-UZ"),
    RUSSIAN("ru", "Русский", "ru-RU"),
    ENGLISH("en", "English", "en-US")
}

// Havo ob-havo modeli
data class WeatherInfo(
    val temperature: Double,
    val description: String,
    val city: String,
    val humidity: Int,
    val windSpeed: Double
)

// Ilova holati
data class AssistantState(
    val isListening: Boolean = false,
    val isProcessing: Boolean = false,
    val isSpeaking: Boolean = false,
    val currentLanguage: AppLanguage = AppLanguage.UZBEK,
    val messages: List<ChatMessage> = emptyList(),
    val error: String? = null,
    val animationState: AnimationState = AnimationState.IDLE
)

enum class AnimationState {
    IDLE, LISTENING, PROCESSING, SPEAKING
}
