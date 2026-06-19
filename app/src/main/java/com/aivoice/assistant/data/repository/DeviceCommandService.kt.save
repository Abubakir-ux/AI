package com.aivoice.assistant.data.repository

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import android.telephony.SmsManager
import com.aivoice.assistant.domain.model.CommandResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceCommandService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null
    private var isFlashlightOn = false

    init {
        try {
            cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            cameraId = cameraManager?.cameraIdList?.firstOrNull()
        } catch (e: Exception) {
            // Camera not available
        }
    }

    fun toggleFlashlight(on: Boolean): CommandResult {
        return try {
            cameraManager?.setTorchMode(cameraId ?: return CommandResult.Error("Kamera topilmadi"), on)
            isFlashlightOn = on
            if (on) CommandResult.Success("Fonar yoqildi")
            else CommandResult.Success("Fonar o'chirildi")
        } catch (e: Exception) {
            CommandResult.Error("Fonar ishlamadi: ${e.message}")
        }
    }

    fun openCamera(): CommandResult {
        return try {
            val intent = Intent("android.media.action.STILL_IMAGE_CAMERA").apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            CommandResult.Success("Kamera ochildi")
        } catch (e: Exception) {
            CommandResult.Error("Kamera ocholmadim")
        }
    }

    fun openApp(packageName: String, appName: String): CommandResult {
        // Maxsus holat: Settings
        if (packageName == "android.settings") {
            return try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                CommandResult.Success("$appName ochildi")
            } catch (e: Exception) {
                CommandResult.Error("$appName ocholmadim")
            }
        }

        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            if (intent != null) {
                context.startActivity(intent)
                CommandResult.Success("$appName ochildi")
            } else {
                // Market ga yo'naltirish
                openPlayStore(packageName)
                CommandResult.Pending("$appName o'rnatilmagan. Do'kon ochilyapti...")
            }
        } catch (e: Exception) {
            CommandResult.Error("$appName ocholmadim")
        }
    }

    private fun openPlayStore(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun getCurrentTime(language: String = "uz"): String {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val minute = now.get(Calendar.MINUTE)
        val minuteStr = if (minute < 10) "0$minute" else "$minute"

        return when (language) {
            "uz" -> "Hozir soat $hour:$minuteStr"
            "ru" -> "Сейчас $hour:$minuteStr"
            else -> "It's $hour:$minuteStr"
        }
    }

    fun makeCall(contactName: String): CommandResult {
        return try {
            val phone = findContactPhone(contactName)
            if (phone != null) {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phone")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                CommandResult.Success("$contactName ga qo'ng'iroq ochilyapti")
            } else {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                CommandResult.Pending("$contactName topilmadi. Telefon ochilyapti...")
            }
        } catch (e: Exception) {
            CommandResult.Error("Qo'ng'iroq qilolmadim")
        }
    }

    fun sendSms(contactName: String, message: String): CommandResult {
        return try {
            val phone = findContactPhone(contactName)
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${phone ?: ""}")
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            CommandResult.Success("SMS yuborish oynasi ochildi")
        } catch (e: Exception) {
            CommandResult.Error("SMS yuborolmadim")
        }
    }

    private fun findContactPhone(name: String): String? {
        return try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                ),
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
                arrayOf("%$name%"),
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    it.getString(it.getColumnIndexOrThrow(
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                    ))
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }
}
