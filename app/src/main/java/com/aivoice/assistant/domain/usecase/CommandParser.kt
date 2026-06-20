package com.aivoice.assistant.domain.usecase

import com.aivoice.assistant.domain.model.VoiceCommand
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandParser @Inject constructor() {

    fun parse(text: String): VoiceCommand {
        val lower = text.lowercase().trim()

        return when {
            isGreeting(lower) -> VoiceCommand.Greeting
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

    private fun isGreeting(text: String) = text.containsAny(
        "salom", "assalomu alaykum", "assalom", "hey ai", "hey eye", "hey",
        "привет", "здравствуй", "хей аи",
        "hello", "hi there"
    ) && text.split(" ").size <= 4

    private fun isFlashlightOn(text: String) = text.containsAny(
        "fonarni yoq", "fonar yoq", "fonarni yoqib", "fonarni yoqing",
        "фонарик включи", "включи фонарик", "включи фонарь",
        "turn on flashlight", "flashlight on", "enable flashlight"
    )

    private fun isFlashlightOff(text: String) = text.containsAny(
        "fonarni o'chir", "fonarni ochir", "fonar o'chir", "fonarni ochirib",
        "выключи фонарик", "выключи фонарь",
        "turn off flashlight", "flashlight off", "disable flashlight"
    )

    private fun isOpenCamera(text: String) = text.containsAny(
        "kamerani och", "kamera och", "kamerani ochib",
        "открой камеру", "камера",
        "open camera", "launch camera"
    )

    private fun isOpenTelegram(text: String) = text.containsAny(
        "telegramni och", "telegram och", "telegramni ochib", "telegram ochish",
        "открой телеграм", "телеграм открой", "запусти телеграм",
        "open telegram", "launch telegram", "start telegram"
    )

    private fun isOpenYouTube(text: String) = text.containsAny(
        "youtubeni och", "youtube och", "ютуб оч", "ютуб открой",
        "открой ютуб", "открой youtube",
        "open youtube", "launch youtube"
    )

    private fun isGetTime(text: String) = text.containsAny(
        "soat nechi", "soat nechchi", "hozir soat", "vaqt qancha",
        "который час", "сколько времени", "время сейчас",
        "what time is it", "current time", "what's the time"
    )

    private fun isGetWeather(text: String) = text.containsAny(
        "ob-havo", "obhavo", "havo qanday", "bugun havo",
        "погода", "какая погода",
        "weather", "what's the weather"
    )

    private fun isMakeCall(text: String) = text.containsAny(
        "qo'ng'iroq qil", "qongiroq qil", "qongiroq", "chaqir",
        "позвони", "набери", "вызови",
        "call", "phone", "dial", "ring"
    )

    private fun isSendSms(text: String) = text.containsAny(
        "sms yubor", "xabar yubor", "sms jo'nat",
        "отправь смс", "напиши смс",
        "send sms", "send message", "text"
    )

    private fun parseMakeCall(text: String): VoiceCommand {
        // Avval "X ga qo'ng'iroq" patterni
        val patterns = listOf(
            Regex("(.+?)\\s+ga\\s+(?:qo'ng'iroq qil|qongiroq qil|qongiroq|chaqir)"),
            Regex("(?:qo'ng'iroq qil|qongiroq qil|chaqir)\\s+(.+)"),
            Regex("(?:позвони|набери|вызови)\\s+(.+)"),
            Regex("(?:call|phone|dial|ring)\\s+(.+)")
        )
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val name = match.groupValues[1].trim()
                    .removeSuffix("ga")
                    .removeSuffix("ni")
                    .trim()
                if (name.isNotEmpty()) return VoiceCommand.MakeCall(name)
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
            if (match != null) return VoiceCommand.SendSms(match.groupValues[1].trim(), "")
        }
        return VoiceCommand.SendSms("", "")
    }

    private fun parseOtherApps(text: String): VoiceCommand? {
        val appMap = mapOf(
            listOf("instagramni och", "instagram och", "instagram",
                   "открой инстаграм", "инстаграм") to
                Pair("com.instagram.android", "Instagram"),
            listOf("whatsappni och", "whatsapp och", "whatsapp", "ватсап",
                   "открой вотсап", "открой ватсап") to
                Pair("com.whatsapp", "WhatsApp"),
            listOf("facebookni och", "facebook och", "facebook",
                   "открой фейсбук") to
                Pair("com.facebook.katana", "Facebook"),
            listOf("tiktokni och", "tiktok och", "tiktok", "тикток",
                   "открой тикток") to
                Pair("com.zhiliaoapp.musically", "TikTok"),
            listOf("xaritani och", "xarita och", "xarita",
                   "открой карты", "карты", "maps") to
                Pair("com.google.android.apps.maps", "Maps"),
            listOf("spotify", "musiqa och", "spotify och") to
                Pair("com.spotify.music", "Spotify"),
            listOf("sozlamalarni och", "sozlamalar", "sozlama",
                   "открой настройки", "настройки", "settings") to
                Pair("android.settings", "Sozlamalar"),
            listOf("kalkulyatorni och", "kalkulyator",
                   "калькулятор", "calculator") to
                Pair("com.google.android.calculator", "Kalkulyator"),
            listOf("gmailni och", "gmail", "pochta",
                   "почта", "email") to
                Pair("com.google.android.gm", "Gmail"),
            listOf("play marketni och", "play market", "market",
                   "плей маркет") to
                Pair("com.android.vending", "Play Market"),
            listOf("chromeni och", "chrome", "brauzer",
                   "браузер", "хром") to
                Pair("com.android.chrome", "Chrome"),
            listOf("galereyani och", "galereya", "rasmlar",
                   "галерея", "gallery") to
                Pair("com.google.android.apps.photos", "Galereya")
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
