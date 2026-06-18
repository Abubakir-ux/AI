# 🎤 AI Voice Assistant — Android

Zamonaviy, ko'p tilli AI ovozli yordamchi. O'zbek, Rus va Ingliz tillarini qo'llab-quvvatlaydi.

---

## 📁 Loyiha Strukturasi

```
AIVoiceAssistant/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/aivoice/assistant/
│           ├── AIVoiceApp.kt                        # Application class (Hilt)
│           ├── data/
│           │   ├── model/
│           │   │   ├── ApiModels.kt                 # OpenAI & Weather API modellari
│           │   │   └── ApiService.kt                # Retrofit interfeyslari
│           │   └── repository/
│           │       ├── AIRepository.kt              # OpenAI & ob-havo so'rovlari
│           │       ├── DeviceCommandService.kt      # Fonar, kamera, qo'ng'iroq
│           │       └── SpeechService.kt             # STT & TTS
│           ├── di/
│           │   └── NetworkModule.kt                 # Hilt DI - Retrofit
│           ├── domain/
│           │   ├── model/
│           │   │   └── Models.kt                    # Domain modellari
│           │   └── usecase/
│           │       └── CommandParser.kt             # Ovoz buyruqlarini parse qilish
│           └── presentation/
│               ├── MainActivity.kt                  # Asosiy faoliyat
│               └── ui/
│                   ├── components/
│                   │   ├── AIAnimation.kt           # AI animatsiyasi (Canvas)
│                   │   ├── ChatMessage.kt           # Chat xabari komponenti
│                   │   └── MicButton.kt             # Mikrofon tugmasi
│                   ├── screens/
│                   │   ├── MainScreen.kt            # Asosiy ekran
│                   │   └── PermissionScreen.kt      # Ruxsat so'rash ekrani
│                   ├── theme/
│                   │   └── Theme.kt                 # Dark theme ranglar
│                   └── viewmodel/
│                       └── AssistantViewModel.kt    # MVVM ViewModel
├── gradle/
│   ├── libs.versions.toml                           # Version catalog
│   └── wrapper/gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 🚀 Android Studio'da Ishga Tushirish

### 1. Loyihani Ochish
```
File → Open → AIVoiceAssistant papkasini tanlang
```

### 2. API Kalitlarini Sozlash
`app/build.gradle.kts` faylida:
```kotlin
buildConfigField("String", "OPENAI_API_KEY", "\"sk-your-openai-key\"")
buildConfigField("String", "WEATHER_API_KEY", "\"your-openweather-key\"")
```

**API kalitlarini olish:**
- OpenAI: https://platform.openai.com/api-keys
- OpenWeather: https://openweathermap.org/api (bepul)

### 3. Sync va Build
```
File → Sync Project with Gradle Files
Build → Make Project
```

### 4. Qurilmada Ishga Tushirish
- USB debugging yoqilgan Android 10+ qurilma
- Run → Run 'app'

---

## 🎯 Buyruqlar Ro'yxati

| Buyruq | Ta'sir |
|--------|--------|
| "Fonarni yoq" | Telefon fonarini yoqadi |
| "Fonarni o'chir" | Fonarni o'chiradi |
| "Kamerani och" | Kamera ilovasini ochadi |
| "Telegramni och" | Telegram ochadi |
| "YouTubeni och" | YouTube ochadi |
| "Soat nechi bo'ldi?" | Hozirgi vaqtni aytadi |
| "Bugun ob-havo qanday?" | Ob-havo ma'lumotini oladi |
| "Akamga qo'ng'iroq qil" | Telefon qilish oynasini ochadi |
| "SMS yubor" | SMS yuborish oynasini ochadi |
| Har qanday savol | OpenAI AI javob beradi |

---

## 🛠 Texnologiyalar

- **Kotlin** + **Jetpack Compose** — UI
- **Hilt** — Dependency Injection
- **MVVM Architecture** — arxitektura
- **SpeechRecognizer API** — ovozni matnga aylantirish
- **TextToSpeech API** — matni ovozga aylantirish
- **Camera2 API** — fonar boshqarish
- **Retrofit + OkHttp** — tarmoq so'rovlari
- **OpenAI GPT-3.5** — AI javoblar
- **OpenWeatherMap API** — ob-havo

---

## ⚙️ Talablar

- Android 10 (API 29)+
- Internet aloqasi (AI va ob-havo uchun)
- Mikrofon
- Google Speech Recognition xizmati

---

## 🌐 Qo'llab-quvvatlangan Tillar

| Til | STT | TTS | Buyruqlar |
|-----|-----|-----|-----------|
| 🇺🇿 O'zbekcha | ✅ | ✅* | ✅ |
| 🇷🇺 Ruscha | ✅ | ✅ | ✅ |
| 🇬🇧 Inglizcha | ✅ | ✅ | ✅ |

*O'zbek TTS — qurilmada mavjud bo'lmasa inglizchaga o'tadi

---

## 📝 Eslatmalar

1. **OpenAI kalit** bo'lmasa, AI savollar ishlamaydi (boshqa buyruqlar ishlaydi)
2. **Ob-havo** uchun alohida OpenWeather kaliti kerak (bepul)
3. **O'zbek nutq tanish** — Google Speech-to-Text qurilmada o'rnatilgan bo'lishi kerak
4. Fonar va kamera uchun maxsus ruxsat so'raladi
