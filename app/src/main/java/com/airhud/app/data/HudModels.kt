package com.airhud.app.data

enum class HudPosition {
    TOP_RIGHT, TOP_LEFT, BOTTOM_RIGHT, BOTTOM_LEFT
}

enum class HudMode {
    HEAD_LOCKED, WORLD_LOCKED, SMOOTH_FOLLOW
}

enum class AiProvider {
    OPENAI, OPENROUTER, OLLAMA, CUSTOM
}

data class WidgetToggles(
    val clockEnabled: Boolean = true,
    val batteryEnabled: Boolean = true,
    val notificationEnabled: Boolean = true,
    val aiEnabled: Boolean = true
)

data class AiConfig(
    val provider: AiProvider = AiProvider.OPENAI,
    val baseUrl: String = "https://api.openai.com/v1",
    val apiKey: String = "",
    val model: String = "gpt-4o-mini"
)

data class HudSettings(
    val hudEnabled: Boolean = false,
    val position: HudPosition = HudPosition.TOP_RIGHT,
    val sizePercent: Int = 25,
    val opacityPercent: Int = 90,
    val mode: HudMode = HudMode.HEAD_LOCKED,
    val widgets: WidgetToggles = WidgetToggles(),
    val aiConfig: AiConfig = AiConfig()
)

data class NotificationInfo(
    val appName: String,
    val title: String,
    val text: String,
    val timestampMillis: Long
)

data class HudRuntimeState(
    val serviceRunning: Boolean = false,
    val clockText: String = "--:--",
    val dateText: String = "",
    val batteryPercent: Int = -1,
    val isCharging: Boolean = false,
    val notification: NotificationInfo? = null,
    val aiPrompt: String = "",
    val aiResponse: String? = null,
    val aiLoading: Boolean = false,
    val aiError: String? = null
)
