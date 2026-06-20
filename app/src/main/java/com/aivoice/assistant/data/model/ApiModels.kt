package com.aivoice.assistant.data.model

import com.google.gson.annotations.SerializedName

// OpenAI API modellari
data class OpenAIRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<OpenAIMessage>,
    @SerializedName("max_tokens") val maxTokens: Int = 500,
    val temperature: Double = 0.7
)

data class OpenAIMessage(
    val role: String,
    val content: String
)

data class OpenAIResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage?
)

data class Choice(
    val index: Int,
    val message: OpenAIMessage,
    @SerializedName("finish_reason") val finishReason: String
)

data class Usage(
    @SerializedName("prompt_tokens") val promptTokens: Int,
    @SerializedName("completion_tokens") val completionTokens: Int,
    @SerializedName("total_tokens") val totalTokens: Int
)

// Gemini API modellari
data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

data class GeminiCandidate(
    val content: GeminiContent?
)

// OpenWeather API modellari
data class WeatherResponse(
    val name: String,
    val main: WeatherMain,
    val weather: List<WeatherDescription>,
    val wind: Wind
)

data class WeatherMain(
    val temp: Double,
    val humidity: Int,
    @SerializedName("feels_like") val feelsLike: Double
)

data class WeatherDescription(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Wind(
    val speed: Double
)
