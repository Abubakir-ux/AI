package com.aivoice.assistant.domain.usecase

import com.aivoice.assistant.domain.model.VoiceCommand
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandParser @Inject constructor() {

    fun parse(text: String): VoiceCommand {
        val lower = text.lowercase().trim()

        return when {
            isFlashlightOn(lower) -> VoiceCommand.FlashlightOn
            isFlashlightOff(lower) -> VoiceCommand.FlashlightOff
            isOpenCamera(lower) -> VoiceCommand.OpenCamera
            isOpenTelegram(lower) -> VoiceCommand.OpenTelegram
            isOpenYouTube(lower) -> VoiceCommand.OpenYouTube
            isGetTime(lower) -> VoiceCommand.GetCurrentTime
            isGetWeather(lower) -> VoiceCommand.GetWeather
            isMakeCall(lower) -> parseMakeCall(lower)
            isSendSms(lower) -> parseSendSms(lower)
            else -> parseOtherApps(lower) ?: VoiceCommand.AskAI(text)
        }
    }

    private fun isFlashlightOn(text: String) = text.containsAny(
        "fonarni yoq", "fonar yoq", "фонарик включи", "включи фонарик",
        "turn on flashlight", "flashlight on"
    )

    private fun isFlashlightOff(text: String) = text.containsAny(
        "fonarni o'chir", "fonarni ochir", "fonar o'chir",
        "выключи фонарик", "turn off flashlight", "flashlight off"
    )

    private fun isOpenCamera(text: String) = text.containsAny(
        "kamerani och", "kamera och", "открой камеру", "open camera"
    )

    private fun isOpenTelegram(text: String) = text.containsAny(
        "telegramni och", "telegram och", "открой телеграм", "open telegram", "telegram"
    )

    private fun isOpenYouTube(text: String) = text.containsAny(
        "youtubeni och", "youtube och", "открой ютуб", "open youtube", "youtube"
    )

    private fun isGetTime(text: String) = text.containsAny(
        "soat nechi", "soat nechchi", "hozir soat", "vaqt",
        "который час", "сколько времени", "what time"
    )

    private fun isGetWeather(text: String) = text.containsAny(
        "ob-havo", "obhavo", "havo qanday", "погода", "weather"
    )

    private fun isMakeCall(text: String) = text.containsAny(
        "qo'ng'iroq qil", "qongiroq", "chaqir", "позвони", "call", "ring"
    )

    private fun isSendSms(text: String) = text.containsAny(
        "sms yubor", "xabar yubor", "отправь смс", "send sms", "send message"
    )

    private fun parseMakeCall(text: String): VoiceCommand {
        val patterns = listOf(
            Regex("(.+?)\\s*ga\\s*(?:qo'ng'iroq|qongiroq|chaqir)"),
            Regex("(?:qo'ng'iroq qil|chaqir)\\s+(.+)"),
            Regex("(?:позвони|набери)\\s+(.+)"),
            Regex("(?:call|ring)\\s+(.+)")
        )
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) return VoiceCommand.MakeCall(match.groupValues[1].trim())
        }
        return VoiceCommand.MakeCall("")
    }

    private fun parseSendSms(text: String): VoiceCommand {
        val patterns = listOf(
            Regex("(?:sms yubor|xabar yubor)\\s+(.+?)\\s+ga"),
            Regex("(?:отправь смс)\\s+(.+)"),
            Regex("(?:send sms|text)\\s+(.+)")
        )
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) return VoiceCommand.SendSms(match.groupValues[1].trim(), "")
        }
        return VoiceCommand.SendSms("", "")
    }

    private fun parseOtherApps(text: String): VoiceCommand? {
        val appMap = mapOf(
            listOf("instagram", "instagramni och", "открой инстаграм") to
                Pair("com.instagram.android", "Instagram"),
            listOf("whatsapp", "whatsappni och", "открой вотсап") to
                Pair("com.whatsapp", "WhatsApp"),
            listOf("facebook", "facebookni och", "открой фейсбук") to
                Pair("com.facebook.katana", "Facebook"),
            listOf("tiktok", "tiktokni och", "открой тикток") to
                Pair("com.zhiliaoapp.musically", "TikTok"),
            listOf("xaritani och", "xarita", "открой карты", "maps") to
                Pair("com.google.android.apps.maps", "Maps"),
            listOf("spotify", "musiqa och") to
                Pair("com.spotify.music", "Spotify"),
            listOf("sozlamalar", "настройки", "settings") to
                Pair("android.settings", "Settings"),
            listOf("calculator", "kalkulyator", "калькулятор") to
                Pair("com.google.android.calculator", "Calculator"),
            listOf("gmail", "почта", "email") to
                Pair("com.google.android.gm", "Gmail"),
            listOf("play market", "плей маркет", "market") to
                Pair("com.android.vending", "Play Market"),
            listOf("chrome", "браузер", "brauzer") to
                Pair("com.android.chrome", "Chrome")
        )

        for ((keywords, appInfo) in appMap) {
            if (text.containsAny(*keywords.toTypedArray())) {
                return VoiceCommand.OpenApp(appInfo.first, appInfo.second)
            }
        }
        return null
    }

    private fun String.containsAny(vararg keywords: String) =
        keywords.any { this.contains(it) }
}
