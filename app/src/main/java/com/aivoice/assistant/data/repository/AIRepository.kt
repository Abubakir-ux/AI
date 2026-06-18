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
    private val openAIService: OpenAIService,
    private val weatherService: WeatherService
) {

    private val conversationHistory = mutableListOf<OpenAIMessage>()

    suspend fun askAI(
        question: String,
        language: String = "uz"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = buildSystemPrompt(language)

            // Tarix bilan xabar yuborish
            val messages = mutableListOf(
                OpenAIMessage("system", systemPrompt)
            )
            messages.addAll(conversationHistory.takeLast(6)) // Oxirgi 6 xabar
            messages.add(OpenAIMessage("user", question))

            val response = openAIService.chat(
                authorization = "Bearer ${BuildConfig.OPENAI_API_KEY}",
                request = OpenAIRequest(messages = messages)
            )

            val answer = response.choices.firstOrNull()?.message?.content
                ?: "Javob topilmadi"

            // Tarixga qo'shish
            conversationHistory.add(OpenAIMessage("user", question))
            conversationHistory.add(OpenAIMessage("assistant", answer))

            // Tarixni cheklash
            if (conversationHistory.size > 20) {
                conversationHistory.removeFirst()
                conversationHistory.removeFirst()
            }

            Result.success(answer)
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
            "uz" -> """Siz AI ovozli yordamchisiz. Qisqa, aniq va foydali javoblar bering.
                |O'zbek tilida muloqot qiling. Javoblar 2-3 jumladan oshmasin.
                |Foydalanuvchiga do'stona munosabatda bo'ling.""".trimMargin()
            "ru" -> """Вы AI голосовой помощник. Давайте краткие, точные и полезные ответы.
                |Общайтесь на русском языке. Ответы не должны превышать 2-3 предложения.
                |Будьте дружелюбны к пользователю.""".trimMargin()
            else -> """You are an AI voice assistant. Give brief, accurate and helpful answers.
                |Communicate in English. Responses should not exceed 2-3 sentences.
                |Be friendly to the user.""".trimMargin()
        }
    }

    fun clearHistory() {
        conversationHistory.clear()
    }
}
