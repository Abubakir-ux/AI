package com.aivoice.assistant.domain.usecase

import com.aivoice.assistant.domain.model.VoiceCommand
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandParser @Inject constructor() {

    fun parse(text: String): VoiceCommand {
        val lower = text.lowercase().trim()

        return when {
            // Fonar
            isFlashlightOn(lower) -> VoiceCommand.FlashlightOn
            isFlashlightOff(lower) -> VoiceCommand.FlashlightOff

            // Kamera
            isOpenCamera(lower) -> VoiceCommand.OpenCamera

            // Telegram
            isOpenTelegram(lower) -> VoiceCommand.OpenTelegram

            // YouTube
            isOpenYouTube(lower) -> VoiceCommand.OpenYouTube

            // Vaqt
            isGetTime(lower) -> VoiceCommand.GetCurrentTime

            // Ob-havo
            isGetWeather(lower) -> VoiceCommand.GetWeather

            // Qo'ng'iroq
            isMakeCall(lower) -> parseMakeCall(lower)

            // SMS
            isSendSms(lower) -> parseSendSms(lower)

            // Boshqa ilovalar
            else -> parseOtherApps(lower) ?: VoiceCommand.AskAI(text)
        }
    }

    private fun isFlashlightOn(text: String): Boolean {
        return text.containsAny(
            "fonarni yoq", "fonарni yoq", "fonar yoq", "фонарни ёқ",
            "включи фонарик", "включи фонарь", "фонарик включи",
            "turn on flashlight", "flashlight on", "enable flashlight"
        )
    }

    private fun isFlashlightOff(text: String): Boolean {
        return text.containsAny(
            "fonarni o'chir", "fonarni ochir", "fonar o'chir", "fonar ochir",
            "выключи фонарик", "выключи фонарь", "фонарик выключи",
            "turn off flashlight", "flashlight off", "disable flashlight"
        )
    }

    private fun isOpenCamera(text: String): Boolean {
        return text.containsAny(
            "kamerani och", "kamera och", "kamerani oч",
            "открой камеру", "камеру открой", "камера",
            "open camera", "camera open"
        )
    }

    private fun isOpenTelegram(text: String): Boolean {
        return text.containsAny(
            "telegramni och", "telegramni ochish", "telegram och",
            "открой телеграм", "телеграм открой", "открыть телеграм",
            "open telegram", "telegram open"
        )
    }

    private fun isOpenYouTube(text: String): Boolean {
        return text.containsAny(
            "youtubeni och", "youtube och", "ютуб оч",
            "открой ютуб", "открой youtube", "ютуб открой",
            "open youtube", "youtube open"
        )
    }

    private fun isGetTime(text: String): Boolean {
        return text.containsAny(
            "soat nechi", "soat nechchi", "hozir soat", "vaqt qancha",
            "который час", "сколько времени", "который сейчас час", "время",
            "what time is it", "current time", "what's the time"
        )
    }

    private fun isGetWeather(text: String): Boolean {
        return text.containsAny(
            "ob-havo", "obhavo", "havo qanday", "bugun havo",
            "погода", "какая погода", "какая сегодня погода",
            "weather", "what's the weather", "how's the weather"
        )
    }

    private fun isMakeCall(text: String): Boolean {
        return text.containsAny(
            "qo'ng'iroq qil", "qongiroq qil", "chaqir",
            "позвони", "позвонить", "набери",
            "call", "phone", "dial"
        )
    }

    private fun isSendSms(text: String): Boolean {
        return text.containsAny(
            "sms yubor", "xabar yubor", "sms jo'nat",
            "отправь смс", "напиши смс", "смс отправь",
            "send sms", "send message", "text"
        )
    }

    private fun parseMakeCall(text: String): VoiceCommand {
        val patterns = listOf(
            Regex("(?:qo'ng'iroq qil|qongiroq qil|chaqir)\\s+(.+)"),
            Regex("(.+?)\\s+(?:ga|ga qo'ng'iroq|chaqir)"),
            Regex("(?:позвони|набери)\\s+(.+)"),
            Regex("(?:call|phone|dial)\\s+(.+)")
        )
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                return VoiceCommand.MakeCall(match.groupValues[1].trim())
            }
        }
        return VoiceCommand.MakeCall("")
    }

    private fun parseSendSms(text: String): VoiceCommand {
        val patterns = listOf(
            Regex("(?:sms yubor|xabar yubor)\\s+(.+?)\\s+(?:ga|uchun)"),
            Regex("(?:отправь смс|напиши смс)\\s+(.+)"),
            Regex("(?:send sms|text)\\s+(.+)")
        )
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                return VoiceCommand.SendSms(match.groupValues[1].trim(), "")
            }
        }
        return VoiceCommand.SendSms("", "")
    }

    private fun parseOtherApps(text: String): VoiceCommand? {
        val appMap = mapOf(
            listOf("instagramni och", "instagram och", "открой инстаграм", "open instagram") 
                to Pair("com.instagram.android", "Instagram"),
            listOf("facebookni och", "facebook och", "открой фейсбук", "open facebook") 
                to Pair("com.facebook.katana", "Facebook"),
            listOf("googleni och", "google och", "открой гугл", "open google") 
                to Pair("com.google.android.googlequicksearchbox", "Google"),
            listOf("xaritani och", "xarita och", "открой карты", "open maps") 
                to Pair("com.google.android.apps.maps", "Maps"),
            listOf("musiqa och", "spotify och", "открой spotify", "open spotify") 
                to Pair("com.spotify.music", "Spotify"),
            listOf("sozlamalarni och", "sozlamalar", "открой настройки", "open settings") 
                to Pair("android.settings", "Settings")
        )

        for ((keywords, appInfo) in appMap) {
            if (text.containsAny(*keywords.toTypedArray())) {
                return VoiceCommand.OpenApp(appInfo.first, appInfo.second)
            }
        }
        return null
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it) }
    }
}
