package com.airhud.app.service

import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.airhud.app.data.HudRepository
import com.airhud.app.data.NotificationInfo
import com.airhud.app.data.SettingsRepository

/**
 * Android's required "Notification Access" integration (spec 8.3 / 10). Reads the
 * latest notification, filters out our own foreground-service notification, and
 * forwards a short-lived summary into [HudRepository] for the overlay to display.
 */
class HudNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return

        val settings = SettingsRepository.getInstance(this).current()
        if (!settings.widgets.notificationEnabled) return
        if (sbn.notification.flags and android.app.Notification.FLAG_ONGOING_EVENT != 0) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString().orEmpty()
        if (title.isBlank() && text.isBlank()) return

        val appName = runCatching {
            val appInfo = packageManager.getApplicationInfo(sbn.packageName, PackageManager.GET_META_DATA)
            packageManager.getApplicationLabel(appInfo).toString()
        }.getOrDefault(sbn.packageName)

        HudRepository.postNotification(
            NotificationInfo(
                appName = appName,
                title = title,
                text = text,
                timestampMillis = System.currentTimeMillis()
            )
        )
    }
}
