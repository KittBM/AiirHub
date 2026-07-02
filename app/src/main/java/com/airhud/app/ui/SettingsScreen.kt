@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.airhud.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.airhud.app.data.AiConfig
import com.airhud.app.data.AiProvider
import com.airhud.app.data.HudMode
import com.airhud.app.data.HudPosition
import com.airhud.app.data.HudRuntimeState
import com.airhud.app.data.HudSettings
import com.airhud.app.data.WidgetToggles

data class PermissionStatus(
    val overlayGranted: Boolean,
    val notificationAccessGranted: Boolean,
    val notificationPermissionGranted: Boolean
)

@Composable
fun SettingsScreen(
    settings: HudSettings,
    runtime: HudRuntimeState,
    permissions: PermissionStatus,
    onRequestOverlayPermission: () -> Unit,
    onRequestNotificationAccess: () -> Unit,
    onRequestNotificationPermission: () -> Unit,
    onToggleHud: (Boolean) -> Unit,
    onPositionChange: (HudPosition) -> Unit,
    onSizeChange: (Int) -> Unit,
    onOpacityChange: (Int) -> Unit,
    onModeChange: (HudMode) -> Unit,
    onWidgetsChange: (WidgetToggles) -> Unit,
    onAiConfigChange: (AiConfig) -> Unit,
    onAskAi: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("AirHUD") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PermissionsCard(
                    permissions = permissions,
                    onRequestOverlayPermission = onRequestOverlayPermission,
                    onRequestNotificationAccess = onRequestNotificationAccess,
                    onRequestNotificationPermission = onRequestNotificationPermission
                )
            }
            item {
                StatusCard(runtime = runtime)
            }
            item {
                HudControlCard(
                    settings = settings,
                    canEnable = permissions.overlayGranted,
                    onToggleHud = onToggleHud,
                    onPositionChange = onPositionChange,
                    onSizeChange = onSizeChange,
                    onOpacityChange = onOpacityChange,
                    onModeChange = onModeChange
                )
            }
            item {
                WidgetsCard(widgets = settings.widgets, onWidgetsChange = onWidgetsChange)
            }
            item {
                AiSettingsCard(aiConfig = settings.aiConfig, onAiConfigChange = onAiConfigChange)
            }
            item {
                AiAskCard(runtime = runtime, onAskAi = onAskAi)
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

private typealias ColumnScope = androidx.compose.foundation.layout.ColumnScope

@Composable
private fun PermissionsCard(
    permissions: PermissionStatus,
    onRequestOverlayPermission: () -> Unit,
    onRequestNotificationAccess: () -> Unit,
    onRequestNotificationPermission: () -> Unit
) {
    SectionCard(title = "Permissions") {
        PermissionRow(
            label = "Display over other apps",
            granted = permissions.overlayGranted,
            onRequest = onRequestOverlayPermission
        )
        PermissionRow(
            label = "Notification access",
            granted = permissions.notificationAccessGranted,
            onRequest = onRequestNotificationAccess
        )
        PermissionRow(
            label = "Post notifications (Android 13+)",
            granted = permissions.notificationPermissionGranted,
            onRequest = onRequestNotificationPermission
        )
    }
}

@Composable
private fun PermissionRow(label: String, granted: Boolean, onRequest: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            Icon(
                imageVector = if (granted) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                contentDescription = null,
                tint = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Text(label, modifier = Modifier.padding(start = 8.dp))
        }
        if (!granted) {
            TextButton(onClick = onRequest) { Text("Grant") }
        }
    }
}

@Composable
private fun StatusCard(runtime: HudRuntimeState) {
    SectionCard(title = "Status") {
        Text(if (runtime.serviceRunning) "HUD service: running" else "HUD service: stopped")
        Text("Clock: ${runtime.clockText} · ${runtime.dateText}")
        Text(
            if (runtime.batteryPercent >= 0) {
                "Battery: ${runtime.batteryPercent}%${if (runtime.isCharging) " (charging)" else ""}"
            } else {
                "Battery: --"
            }
        )
        runtime.notification?.let {
            Text("Last notification: ${it.appName} — ${it.title}")
        }
    }
}

@Composable
private fun HudControlCard(
    settings: HudSettings,
    canEnable: Boolean,
    onToggleHud: (Boolean) -> Unit,
    onPositionChange: (HudPosition) -> Unit,
    onSizeChange: (Int) -> Unit,
    onOpacityChange: (Int) -> Unit,
    onModeChange: (HudMode) -> Unit
) {
    SectionCard(title = "Corner HUD") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Enable HUD")
            Switch(checked = settings.hudEnabled, enabled = canEnable, onCheckedChange = onToggleHud)
        }
        if (!canEnable) {
            Text(
                "Grant the overlay permission above to enable the HUD.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Text("Position", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HudPosition.entries.forEach { position ->
                FilterChip(
                    selected = settings.position == position,
                    onClick = { onPositionChange(position) },
                    label = { Text(position.name.replace('_', ' ')) }
                )
            }
        }

        Text("Mode", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HudMode.entries.forEach { mode ->
                FilterChip(
                    selected = settings.mode == mode,
                    onClick = { onModeChange(mode) },
                    label = { Text(mode.name.replace('_', ' ')) }
                )
            }
        }
        Text(
            "Note: without XREAL SDK head-tracking, this phone overlay always behaves as Head-Locked. " +
                "World-Locked and Smooth Follow are stored for when the spatial renderer is wired up.",
            style = MaterialTheme.typography.bodySmall
        )

        Text("Size: ${settings.sizePercent}%", style = MaterialTheme.typography.labelLarge)
        Slider(
            value = settings.sizePercent.toFloat(),
            onValueChange = { onSizeChange(it.toInt()) },
            valueRange = 15f..60f
        )

        Text("Opacity: ${settings.opacityPercent}%", style = MaterialTheme.typography.labelLarge)
        Slider(
            value = settings.opacityPercent.toFloat(),
            onValueChange = { onOpacityChange(it.toInt()) },
            valueRange = 10f..100f
        )
    }
}

@Composable
private fun WidgetsCard(widgets: WidgetToggles, onWidgetsChange: (WidgetToggles) -> Unit) {
    SectionCard(title = "Widgets") {
        WidgetRow("Clock", widgets.clockEnabled) {
            onWidgetsChange(widgets.copy(clockEnabled = it))
        }
        WidgetRow("Battery", widgets.batteryEnabled) {
            onWidgetsChange(widgets.copy(batteryEnabled = it))
        }
        WidgetRow("Notification", widgets.notificationEnabled) {
            onWidgetsChange(widgets.copy(notificationEnabled = it))
        }
        WidgetRow("AI Assistant", widgets.aiEnabled) {
            onWidgetsChange(widgets.copy(aiEnabled = it))
        }
    }
}

@Composable
private fun WidgetRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun AiSettingsCard(aiConfig: AiConfig, onAiConfigChange: (AiConfig) -> Unit) {
    SectionCard(title = "AI Assistant provider") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AiProvider.entries.forEach { provider ->
                FilterChip(
                    selected = aiConfig.provider == provider,
                    onClick = {
                        val defaults = defaultsFor(provider)
                        onAiConfigChange(aiConfig.copy(provider = provider, baseUrl = defaults))
                    },
                    label = { Text(provider.name) }
                )
            }
        }
        OutlinedTextField(
            value = aiConfig.baseUrl,
            onValueChange = { onAiConfigChange(aiConfig.copy(baseUrl = it)) },
            label = { Text("Base URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = aiConfig.apiKey,
            onValueChange = { onAiConfigChange(aiConfig.copy(apiKey = it)) },
            label = { Text("API key (blank for local Ollama)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        OutlinedTextField(
            value = aiConfig.model,
            onValueChange = { onAiConfigChange(aiConfig.copy(model = it)) },
            label = { Text("Model") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

private fun defaultsFor(provider: AiProvider): String = when (provider) {
    AiProvider.OPENAI -> "https://api.openai.com/v1"
    AiProvider.OPENROUTER -> "https://openrouter.ai/api/v1"
    AiProvider.OLLAMA -> "http://localhost:11434/v1"
    AiProvider.CUSTOM -> ""
}

@Composable
private fun AiAskCard(runtime: HudRuntimeState, onAskAi: (String) -> Unit) {
    var prompt by remember { mutableStateOf("") }
    SectionCard(title = "Ask AI (shows on HUD)") {
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Question") },
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(
            onClick = { if (prompt.isNotBlank()) onAskAi(prompt) },
            enabled = !runtime.aiLoading
        ) {
            Text(if (runtime.aiLoading) "Sending..." else "Send to HUD")
        }
        runtime.aiResponse?.let { Text("Last answer: $it") }
        runtime.aiError?.let {
            Text("Error: $it", color = MaterialTheme.colorScheme.error)
        }
    }
}
