package com.airhud.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Persists [HudSettings] to SharedPreferences and exposes them as a hot StateFlow so
 * the settings UI, the overlay service, and the notification listener all observe the
 * same live configuration.
 */
class SettingsRepository private constructor(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<HudSettings> = _settings

    fun current(): HudSettings = _settings.value

    fun update(transform: (HudSettings) -> HudSettings) {
        val next = transform(_settings.value)
        _settings.update { next }
        save(next)
    }

    private fun load(): HudSettings {
        return HudSettings(
            hudEnabled = prefs.getBoolean(KEY_HUD_ENABLED, false),
            position = runCatching { HudPosition.valueOf(prefs.getString(KEY_POSITION, null)!!) }
                .getOrDefault(HudPosition.TOP_RIGHT),
            sizePercent = prefs.getInt(KEY_SIZE, 25),
            opacityPercent = prefs.getInt(KEY_OPACITY, 90),
            mode = runCatching { HudMode.valueOf(prefs.getString(KEY_MODE, null)!!) }
                .getOrDefault(HudMode.HEAD_LOCKED),
            widgets = WidgetToggles(
                clockEnabled = prefs.getBoolean(KEY_W_CLOCK, true),
                batteryEnabled = prefs.getBoolean(KEY_W_BATTERY, true),
                notificationEnabled = prefs.getBoolean(KEY_W_NOTIF, true),
                aiEnabled = prefs.getBoolean(KEY_W_AI, true)
            ),
            aiConfig = AiConfig(
                provider = runCatching { AiProvider.valueOf(prefs.getString(KEY_AI_PROVIDER, null)!!) }
                    .getOrDefault(AiProvider.OPENAI),
                baseUrl = prefs.getString(KEY_AI_BASE_URL, null) ?: "https://api.openai.com/v1",
                apiKey = prefs.getString(KEY_AI_API_KEY, null) ?: "",
                model = prefs.getString(KEY_AI_MODEL, null) ?: "gpt-4o-mini"
            )
        )
    }

    private fun save(settings: HudSettings) {
        prefs.edit().apply {
            putBoolean(KEY_HUD_ENABLED, settings.hudEnabled)
            putString(KEY_POSITION, settings.position.name)
            putInt(KEY_SIZE, settings.sizePercent)
            putInt(KEY_OPACITY, settings.opacityPercent)
            putString(KEY_MODE, settings.mode.name)
            putBoolean(KEY_W_CLOCK, settings.widgets.clockEnabled)
            putBoolean(KEY_W_BATTERY, settings.widgets.batteryEnabled)
            putBoolean(KEY_W_NOTIF, settings.widgets.notificationEnabled)
            putBoolean(KEY_W_AI, settings.widgets.aiEnabled)
            putString(KEY_AI_PROVIDER, settings.aiConfig.provider.name)
            putString(KEY_AI_BASE_URL, settings.aiConfig.baseUrl)
            putString(KEY_AI_API_KEY, settings.aiConfig.apiKey)
            putString(KEY_AI_MODEL, settings.aiConfig.model)
        }.apply()
    }

    companion object {
        private const val PREFS_NAME = "airhud_settings"
        private const val KEY_HUD_ENABLED = "hud_enabled"
        private const val KEY_POSITION = "position"
        private const val KEY_SIZE = "size_percent"
        private const val KEY_OPACITY = "opacity_percent"
        private const val KEY_MODE = "mode"
        private const val KEY_W_CLOCK = "w_clock"
        private const val KEY_W_BATTERY = "w_battery"
        private const val KEY_W_NOTIF = "w_notif"
        private const val KEY_W_AI = "w_ai"
        private const val KEY_AI_PROVIDER = "ai_provider"
        private const val KEY_AI_BASE_URL = "ai_base_url"
        private const val KEY_AI_API_KEY = "ai_api_key"
        private const val KEY_AI_MODEL = "ai_model"

        @Volatile
        private var instance: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository =
            instance ?: synchronized(this) {
                instance ?: SettingsRepository(context).also { instance = it }
            }
    }
}
