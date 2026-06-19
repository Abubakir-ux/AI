package com.aivoice.assistant.presentation

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.aivoice.assistant.presentation.ui.screens.MainScreen
import com.aivoice.assistant.presentation.ui.screens.PermissionScreen
import com.aivoice.assistant.presentation.ui.theme.AIVoiceTheme
import com.aivoice.assistant.presentation.viewmodel.AssistantViewModel
import com.aivoice.assistant.service.WakeWordService
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: AssistantViewModel by viewModels()

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startWakeWordService()
        if (intent.getBooleanExtra("wake_word_triggered", false)) {
            viewModel.startListening()
        }
        setContent {
            AIVoiceTheme {
                val permissionsState = rememberMultiplePermissionsState(
                    permissions = listOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.CALL_PHONE
                    )
                )
                val uiState by viewModel.uiState.collectAsState()
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    if (permissionsState.allPermissionsGranted) {
                        MainScreen(
                            uiState = uiState,
                            onStartListening = { viewModel.startListening() },
                            onStopListening = { viewModel.stopListening() },
                            onLanguageChange = { viewModel.changeLanguage(it) },
                            onClearChat = { viewModel.clearChat() },
                            onSendMessage = { viewModel.sendTextMessage(it) },
                            modifier = Modifier.padding(paddingValues)
                        )
                    } else {
                        PermissionScreen(
                            onRequestPermissions = { permissionsState.launchMultiplePermissionRequest() },
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra("wake_word_triggered", false)) {
            viewModel.startListening()
        }
    }

    private fun startWakeWordService() {
        try {
            val serviceIntent = Intent(this, WakeWordService::class.java)
            startForegroundService(serviceIntent)
        } catch (e: Exception) {}
    }
}
