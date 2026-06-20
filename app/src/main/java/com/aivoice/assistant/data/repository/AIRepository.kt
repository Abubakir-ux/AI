package com.aivoice.assistant.data.repository

import com.aivoice.assistant.BuildConfig
import com.aivoice.assistant.data.model.*
import com.aivoice.assistant.domain.model.WeatherInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepository @Inject constructor(
    private val geminiService: GeminiService,
    private val weatherService: WeatherService
) {

    suspend fun askAI(
        question: String,
        language: String = "uz"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = buildSystemPrompt(language)
            val fullPrompt = "$systemPrompt\n\nSavol: $question"

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = fullPrompt)))
                )
            )

            val response = geminiService.generateContent(
                apiKey = BuildConfig.GEMINI_API_KEY,
                request = request
            )

            val answer = response.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text
                ?: "Javob topilmadi"

            Result.success(answer.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWeather(city: String = "Tashkent"): Result<WeatherInfo> =
        withContext(Dispatchers.IO) {
            try {
                val response = weatherService.getWeather(
                    city = city,
                    apiKey = BuildConfig.WEATHER_API_KEY
                )
                Result.success(
                    WeatherInfo(
                        temperature = response.main.temp,
                        description = response.weather.firstOrNull()?.description ?: "",
                        city = response.name,
                        humidity = response.main.humidity,
                        windSpeed = response.wind.speed
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun buildSystemPrompt(language: String): String {
        return when (language) {
            "uz" -> "Siz AI ovozli yordamchisiz. Qisqa, aniq va foydali javob bering o'zbek tilida. 2-3 jumladan oshmasin."
            "ru" -> "Вы AI голосовой помощник. Дайте краткий ответ на русском языке. Не более 2-3 предложений."
            else -> "You are an AI voice assistant. Give a brief answer in English. No more than 2-3 sentences."
        }
    }

    fun clearHistory() {}
}
