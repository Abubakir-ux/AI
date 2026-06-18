package com.aivoice.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aivoice.assistant.data.repository.*
import com.aivoice.assistant.domain.model.*
import com.aivoice.assistant.domain.usecase.CommandParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val speechService: SpeechService,
    private val deviceCommandService: DeviceCommandService,
    private val aiRepository: AIRepository,
    private val commandParser: CommandParser
) : ViewModel() {

    private val _uiState = MutableStateFlow(AssistantState())
    val uiState: StateFlow<AssistantState> = _uiState.asStateFlow()

    init {
        observeSpeechEvents()
        initTts()
    }

    private fun initTts() {
        viewModelScope.launch {
            delay(500)
            speechService.setLanguage(_uiState.value.currentLanguage)
        }
    }

    private fun observeSpeechEvents() {
        viewModelScope.launch {
            speechService.speechEvents.collect { event ->
                when (event) {
                    is SpeechEvent.ReadyForSpeech -> {
                        updateState { it.copy(
                            isListening = true,
                            animationState = AnimationState.LISTENING
                        )}
                    }
                    is SpeechEvent.Result -> {
                        updateState { it.copy(
                            isListening = false,
                            animationState = AnimationState.PROCESSING
                        )}
                        processVoiceInput(event.text)
                    }
                    is SpeechEvent.Error -> {
                        updateState { it.copy(
                            isListening = false,
                            isProcessing = false,
                            animationState = AnimationState.IDLE,
                            error = event.message
                        )}
                        delay(2000)
                        updateState { it.copy(error = null) }
                    }
                    is SpeechEvent.SpeakingFinished -> {
                        updateState { it.copy(
                            isSpeaking = false,
                            animationState = AnimationState.IDLE
                        )}
                    }
                    else -> {}
                }
            }
        }
    }

    fun startListening() {
        if (_uiState.value.isListening || _uiState.value.isSpeaking) return
        speechService.stopSpeaking()
        speechService.startListening(_uiState.value.currentLanguage)
        updateState { it.copy(animationState = AnimationState.LISTENING) }
    }

    fun stopListening() {
        speechService.stopListening()
        updateState { it.copy(
            isListening = false,
            animationState = AnimationState.IDLE
        )}
    }

    private fun processVoiceInput(text: String) {
        // Foydalanuvchi xabarini qo'shish
        addMessage(ChatMessage(text = text, isUser = true))

        val command = commandParser.parse(text)
        executeCommand(command, text)
    }

    private fun executeCommand(command: VoiceCommand, originalText: String) {
        viewModelScope.launch {
            updateState { it.copy(isProcessing = true, animationState = AnimationState.PROCESSING) }

            val lang = _uiState.value.currentLanguage.code

            when (command) {
                is VoiceCommand.FlashlightOn -> {
                    val preMsg = getPreMessage("flashlight_on", lang)
                    speakAndAddMessage(preMsg)
                    delay(500)
                    val result = deviceCommandService.toggleFlashlight(true)
                    handleResult(result, lang)
                }
                is VoiceCommand.FlashlightOff -> {
                    val preMsg = getPreMessage("flashlight_off", lang)
                    speakAndAddMessage(preMsg)
                    delay(500)
                    val result = deviceCommandService.toggleFlashlight(false)
                    handleResult(result, lang)
                }
                is VoiceCommand.OpenCamera -> {
                    val preMsg = getPreMessage("open_camera", lang)
                    speakAndAddMessage(preMsg)
                    delay(500)
                    val result = deviceCommandService.openCamera()
                    handleResult(result, lang)
                }
                is VoiceCommand.OpenTelegram -> {
                    val preMsg = getPreMessage("open_telegram", lang)
                    speakAndAddMessage(preMsg)
                    delay(500)
                    val result = deviceCommandService.openApp("org.telegram.messenger", "Telegram")
                    handleResult(result, lang)
                }
                is VoiceCommand.OpenYouTube -> {
                    val preMsg = getPreMessage("open_youtube", lang)
                    speakAndAddMessage(preMsg)
                    delay(500)
                    val result = deviceCommandService.openApp("com.google.android.youtube", "YouTube")
                    handleResult(result, lang)
                }
                is VoiceCommand.GetCurrentTime -> {
                    val timeMsg = deviceCommandService.getCurrentTime(lang)
                    speakAndAddMessage(timeMsg)
                }
                is VoiceCommand.GetWeather -> {
                    val preMsg = getPreMessage("getting_weather", lang)
                    speakAndAddMessage(preMsg)
                    fetchWeather(lang)
                }
                is VoiceCommand.MakeCall -> {
                    val name = command.contactName.ifEmpty {
                        when (lang) {
                            "uz" -> "kimgadir"
                            "ru" -> "кому-то"
                            else -> "someone"
                        }
                    }
                    val preMsg = when (lang) {
                        "uz" -> "$name ga qo'ng'iroq qilaman"
                        "ru" -> "Звоню $name"
                        else -> "Calling $name"
                    }
                    speakAndAddMessage(preMsg)
                    delay(800)
                    val result = deviceCommandService.makeCall(command.contactName)
                    if (result is CommandResult.Pending) {
                        addMessage(ChatMessage(text = result.message, isUser = false))
                    }
                }
                is VoiceCommand.SendSms -> {
                    val preMsg = when (lang) {
                        "uz" -> "SMS yuborish oynasini ochaman"
                        "ru" -> "Открываю окно SMS"
                        else -> "Opening SMS window"
                    }
                    speakAndAddMessage(preMsg)
                    delay(800)
                    deviceCommandService.sendSms(command.contactName, command.message)
                }
                is VoiceCommand.OpenApp -> {
                    val preMsg = when (lang) {
                        "uz" -> "${command.appName} ochilmoqda"
                        "ru" -> "Открываю ${command.appName}"
                        else -> "Opening ${command.appName}"
                    }
                    speakAndAddMessage(preMsg)
                    delay(500)
                    val result = deviceCommandService.openApp(command.packageName, command.appName)
                    handleResult(result, lang)
                }
                is VoiceCommand.AskAI -> {
                    val preMsg = getPreMessage("thinking", lang)
                    updateState { it.copy(animationState = AnimationState.PROCESSING) }
                    addMessage(ChatMessage(text = preMsg, isUser = false))
                    fetchAIAnswer(originalText, lang)
                }
                is VoiceCommand.Unknown -> {
                    val msg = when (lang) {
                        "uz" -> "Kechirasiz, tushunmadim. Qaytadan ayting."
                        "ru" -> "Извините, не понял. Повторите, пожалуйста."
                        else -> "Sorry, I didn't understand. Please repeat."
                    }
                    speakAndAddMessage(msg)
                }
            }

            updateState { it.copy(isProcessing = false) }
        }
    }

    private suspend fun fetchWeather(lang: String) {
        val result = aiRepository.getWeather("Tashkent")
        val message = result.fold(
            onSuccess = { weather ->
                when (lang) {
                    "uz" -> "${weather.city}da havo: ${weather.temperature.toInt()}°C, " +
                            "${weather.description}. Namlik: ${weather.humidity}%"
                    "ru" -> "В ${weather.city}: ${weather.temperature.toInt()}°C, " +
                            "${weather.description}. Влажность: ${weather.humidity}%"
                    else -> "${weather.city}: ${weather.temperature.toInt()}°C, " +
                            "${weather.description}. Humidity: ${weather.humidity}%"
                }
            },
            onFailure = {
                when (lang) {
                    "uz" -> "Ob-havo ma'lumotini ololmadim"
                    "ru" -> "Не удалось получить данные о погоде"
                    else -> "Couldn't get weather data"
                }
            }
        )
        speakAndAddMessage(message)
    }

    private suspend fun fetchAIAnswer(question: String, lang: String) {
        // Oxirgi "o'ylamoqdaman" xabarni olib tashlash
        val currentMessages = _uiState.value.messages.toMutableList()
        if (currentMessages.lastOrNull()?.isUser == false) {
            currentMessages.removeLast()
        }

        val result = aiRepository.askAI(question, lang)
        val message = result.fold(
            onSuccess = { it },
            onFailure = {
                when (lang) {
                    "uz" -> "Xatolik yuz berdi. Internet aloqasini tekshiring."
                    "ru" -> "Произошла ошибка. Проверьте интернет-соединение."
                    else -> "An error occurred. Please check your internet connection."
                }
            }
        )
        updateState { it.copy(messages = currentMessages) }
        speakAndAddMessage(message)
    }

    private fun speakAndAddMessage(text: String) {
        addMessage(ChatMessage(text = text, isUser = false))
        updateState { it.copy(isSpeaking = true, animationState = AnimationState.SPEAKING) }
        speechService.speak(text)
    }

    private fun handleResult(result: CommandResult, lang: String) {
        when (result) {
            is CommandResult.Success -> {
                // Muvaffaqiyatli bajarildi - qo'shimcha xabar shart emas
            }
            is CommandResult.Error -> {
                val errorMsg = when (lang) {
                    "uz" -> "Xatolik: ${result.message}"
                    "ru" -> "Ошибка: ${result.message}"
                    else -> "Error: ${result.message}"
                }
                speakAndAddMessage(errorMsg)
            }
            is CommandResult.Pending -> {
                addMessage(ChatMessage(text = result.message, isUser = false))
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        updateState { state ->
            state.copy(messages = state.messages + message)
        }
    }

    fun changeLanguage(language: AppLanguage) {
        updateState { it.copy(currentLanguage = language) }
        speechService.setLanguage(language)

        val greeting = when (language) {
            AppLanguage.UZBEK -> "Til o'zbekchaga o'zgartirildi"
            AppLanguage.RUSSIAN -> "Язык изменён на русский"
            AppLanguage.ENGLISH -> "Language changed to English"
        }
        speakAndAddMessage(greeting)
    }

    fun clearChat() {
        updateState { it.copy(messages = emptyList()) }
        aiRepository.clearHistory()
    }

    fun sendTextMessage(text: String) {
        if (text.isBlank()) return
        addMessage(ChatMessage(text = text, isUser = true))
        val command = commandParser.parse(text)
        executeCommand(command, text)
    }

    private fun getPreMessage(key: String, lang: String): String {
        return when (key) {
            "flashlight_on" -> when (lang) {
                "uz" -> "Fonarni yoqaman"
                "ru" -> "Включаю фонарик"
                else -> "Turning on flashlight"
            }
            "flashlight_off" -> when (lang) {
                "uz" -> "Fonarni o'chiraman"
                "ru" -> "Выключаю фонарик"
                else -> "Turning off flashlight"
            }
            "open_camera" -> when (lang) {
                "uz" -> "Kamerani ochaman"
                "ru" -> "Открываю камеру"
                else -> "Opening camera"
            }
            "open_telegram" -> when (lang) {
                "uz" -> "Telegramni ochaman"
                "ru" -> "Открываю Telegram"
                else -> "Opening Telegram"
            }
            "open_youtube" -> when (lang) {
                "uz" -> "YouTubeni ochaman"
                "ru" -> "Открываю YouTube"
                else -> "Opening YouTube"
            }
            "getting_weather" -> when (lang) {
                "uz" -> "Ob-havo ma'lumotini olyapman..."
                "ru" -> "Получаю данные о погоде..."
                else -> "Getting weather data..."
            }
            "thinking" -> when (lang) {
                "uz" -> "O'ylamoqdaman..."
                "ru" -> "Думаю..."
                else -> "Thinking..."
            }
            else -> ""
        }
    }

    private fun updateState(transform: (AssistantState) -> AssistantState) {
        _uiState.update(transform)
    }

    override fun onCleared() {
        super.onCleared()
        speechService.destroy()
    }
}
