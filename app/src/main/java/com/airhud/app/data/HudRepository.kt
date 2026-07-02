package com.airhud.app.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Process-wide singleton that mirrors the "HUD Data Layer / State Stream" from the spec.
 * The overlay service, notification listener, battery receiver and settings UI all
 * read/write through this single StateFlow instead of a real IPC bus, since everything
 * runs in the same app process on this Android-only MVP.
 */
object HudRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var notificationClearJob: Job? = null
    private var aiClearJob: Job? = null

    private val _state = MutableStateFlow(HudRuntimeState())
    val state: StateFlow<HudRuntimeState> = _state

    fun setServiceRunning(running: Boolean) {
        _state.update { it.copy(serviceRunning = running) }
    }

    fun updateClock(clockText: String, dateText: String) {
        _state.update { it.copy(clockText = clockText, dateText = dateText) }
    }

    fun updateBattery(percent: Int, isCharging: Boolean) {
        _state.update { it.copy(batteryPercent = percent, isCharging = isCharging) }
    }

    fun postNotification(info: NotificationInfo, autoClearMillis: Long = 5000L) {
        notificationClearJob?.cancel()
        _state.update { it.copy(notification = info) }
        notificationClearJob = scope.launch {
            delay(autoClearMillis)
            _state.update { current ->
                if (current.notification?.timestampMillis == info.timestampMillis) {
                    current.copy(notification = null)
                } else {
                    current
                }
            }
        }
    }

    fun setAiPrompt(prompt: String) {
        _state.update { it.copy(aiPrompt = prompt) }
    }

    fun setAiLoading() {
        aiClearJob?.cancel()
        _state.update { it.copy(aiLoading = true, aiError = null) }
    }

    fun setAiResult(response: String, autoClearMillis: Long = 20_000L) {
        _state.update { it.copy(aiLoading = false, aiResponse = response, aiError = null) }
        aiClearJob?.cancel()
        aiClearJob = scope.launch {
            delay(autoClearMillis)
            _state.update { it.copy(aiResponse = null) }
        }
    }

    fun setAiError(message: String) {
        _state.update { it.copy(aiLoading = false, aiError = message) }
    }

    fun dismissNotification() {
        notificationClearJob?.cancel()
        _state.update { it.copy(notification = null) }
    }

    fun dismissAiResponse() {
        aiClearJob?.cancel()
        _state.update { it.copy(aiResponse = null, aiError = null) }
    }
}
