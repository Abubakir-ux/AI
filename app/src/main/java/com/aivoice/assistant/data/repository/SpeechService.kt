package com.aivoice.assistant.data.repository

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.aivoice.assistant.domain.model.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

sealed class SpeechEvent {
    data class Result(val text: String) : SpeechEvent()
    data class Error(val message: String) : SpeechEvent()
    object RmsChanged : SpeechEvent()
    object ReadyForSpeech : SpeechEvent()
    object SpeakingFinished : SpeechEvent()
}

@Singleton
class SpeechService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _speechEvents = Channel<SpeechEvent>(Channel.BUFFERED)
    val speechEvents: Flow<SpeechEvent> = _speechEvents.receiveAsFlow()

    private var onSpeakFinished: (() -> Unit)? = null

    init {
        initTts()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        _speechEvents.trySend(SpeechEvent.SpeakingFinished)
                        onSpeakFinished?.invoke()
                    }
                    override fun onError(utteranceId: String?) {
                        _speechEvents.trySend(SpeechEvent.SpeakingFinished)
                    }
                })
            }
        }
    }

    fun setLanguage(language: AppLanguage) {
        if (isTtsReady) {
            val locale = when (language) {
                AppLanguage.UZBEK -> Locale("uz", "UZ")
                AppLanguage.RUSSIAN -> Locale("ru", "RU")
                AppLanguage.ENGLISH -> Locale.ENGLISH
            }
            val result = textToSpeech?.setLanguage(locale)
            if (result == TextToSpeech.LANG_NOT_SUPPORTED ||
                result == TextToSpeech.LANG_MISSING_DATA) {
                // Fallback to English
                textToSpeech?.setLanguage(Locale.ENGLISH)
            }
            textToSpeech?.setSpeechRate(0.9f)
            textToSpeech?.setPitch(1.0f)
        }
    }

    fun speak(text: String, onFinished: (() -> Unit)? = null) {
        if (!isTtsReady) return
        onSpeakFinished = onFinished
        textToSpeech?.stop()
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_id")
        }
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "tts_id")
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
    }

    fun startListening(language: AppLanguage) {
        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _speechEvents.trySend(SpeechEvent.ReadyForSpeech)
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {
                _speechEvents.trySend(SpeechEvent.RmsChanged)
            }
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                val msg = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Mikrofon xatosi"
                    SpeechRecognizer.ERROR_NETWORK -> "Tarmoq xatosi"
                    SpeechRecognizer.ERROR_NO_MATCH -> "Nutq tanilmadi"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Nutq kelmadi"
                    else -> "Noma'lum xato: $error"
                }
                _speechEvents.trySend(SpeechEvent.Error(msg))
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                if (text.isNotEmpty()) {
                    _speechEvents.trySend(SpeechEvent.Result(text))
                } else {
                    _speechEvents.trySend(SpeechEvent.Error("Nutq aniqlanmadi"))
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val locale = when (language) {
            AppLanguage.UZBEK -> "uz-UZ"
            AppLanguage.RUSSIAN -> "ru-RU"
            AppLanguage.ENGLISH -> "en-US"
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    fun destroy() {
        speechRecognizer?.destroy()
        textToSpeech?.shutdown()
    }
}
