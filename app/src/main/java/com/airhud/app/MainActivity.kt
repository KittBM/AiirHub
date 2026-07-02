package com.airhud.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.airhud.app.ai.AiClient
import com.airhud.app.data.HudRepository
import com.airhud.app.data.SettingsRepository
import com.airhud.app.service.HudOverlayService
import com.airhud.app.ui.PermissionStatus
import com.airhud.app.ui.SettingsScreen
import com.airhud.app.ui.theme.AirHudTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var settingsRepository: SettingsRepository
    private val aiClient = AiClient()

    private val permissionState by lazy { mutableStateOf(computePermissionStatus()) }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            permissionState.value = computePermissionStatus()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsRepository = SettingsRepository.getInstance(this)

        // The service only runs while its process is alive; if the app process was
        // killed (reinstall, swipe-away, reboot) while HUD was enabled, restart it
        // here so "Enable HUD" stays true to what's actually on screen.
        if (settingsRepository.current().hudEnabled && Settings.canDrawOverlays(this)) {
            HudOverlayService.start(this)
        }

        setContent {
            AirHudTheme {
                val settings by settingsRepository.settings.collectAsStateWithLifecycle()
                val runtime by HudRepository.state.collectAsStateWithLifecycle()
                val permissions by permissionState
                val scope = rememberCoroutineScope()

                SettingsScreen(
                    settings = settings,
                    runtime = runtime,
                    permissions = permissions,
                    onRequestOverlayPermission = { requestOverlayPermission() },
                    onRequestNotificationAccess = { requestNotificationAccess() },
                    onRequestNotificationPermission = { requestNotificationPermission() },
                    onToggleHud = { enabled -> toggleHud(enabled) },
                    onPositionChange = { position ->
                        settingsRepository.update { it.copy(position = position) }
                    },
                    onSizeChange = { size ->
                        settingsRepository.update { it.copy(sizePercent = size) }
                    },
                    onOpacityChange = { opacity ->
                        settingsRepository.update { it.copy(opacityPercent = opacity) }
                    },
                    onModeChange = { mode ->
                        settingsRepository.update { it.copy(mode = mode) }
                    },
                    onWidgetsChange = { widgets ->
                        settingsRepository.update { it.copy(widgets = widgets) }
                    },
                    onAiConfigChange = { aiConfig ->
                        settingsRepository.update { it.copy(aiConfig = aiConfig) }
                    },
                    onAskAi = { prompt -> askAi(scope, prompt) }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        permissionState.value = computePermissionStatus()
    }

    private fun toggleHud(enabled: Boolean) {
        settingsRepository.update { it.copy(hudEnabled = enabled) }
        if (enabled) HudOverlayService.start(this) else HudOverlayService.stop(this)
    }

    private fun askAi(scope: CoroutineScope, prompt: String) {
        val config = settingsRepository.current().aiConfig
        HudRepository.setAiPrompt(prompt)
        HudRepository.setAiLoading()
        scope.launch {
            aiClient.ask(prompt, config).fold(
                onSuccess = { HudRepository.setAiResult(it) },
                onFailure = { HudRepository.setAiError(it.message ?: "Unknown error") }
            )
        }
    }

    private fun requestOverlayPermission() {
        startActivity(
            Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        )
    }

    private fun requestNotificationAccess() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun computePermissionStatus(): PermissionStatus {
        val overlayGranted = Settings.canDrawOverlays(this)
        val notificationAccessGranted =
            NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
        val notificationPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        return PermissionStatus(overlayGranted, notificationAccessGranted, notificationPermissionGranted)
    }
}
