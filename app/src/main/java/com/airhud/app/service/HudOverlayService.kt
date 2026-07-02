package com.airhud.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import android.util.TypedValue
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.airhud.app.MainActivity
import com.airhud.app.R
import com.airhud.app.data.HudRepository
import com.airhud.app.data.HudSettings
import com.airhud.app.data.SettingsRepository
import com.airhud.app.overlay.HudOverlayView
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Renders the always-on-top corner HUD via WindowManager. This is the phone-only
 * stand-in for the "Spatial Renderer / XREAL SDK" layer described in the spec: on a
 * real XREAL Air the panel would be head-locked in 3D space, here it's head-locked
 * to a screen corner, which is the closest phone-only analog.
 */
class HudOverlayService : LifecycleService() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: HudOverlayView
    private lateinit var settingsRepository: SettingsRepository
    private var layoutParams: WindowManager.LayoutParams? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val percent = if (level >= 0 && scale > 0) (level * 100 / scale) else -1
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
            HudRepository.updateBattery(percent, charging)
        }
    }

    override fun onCreate() {
        super.onCreate()

        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        settingsRepository = SettingsRepository.getInstance(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        overlayView = HudOverlayView(this)

        addOverlayView(settingsRepository.current())
        startForeground(NOTIFICATION_ID, buildNotification())
        HudRepository.setServiceRunning(true)

        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        startClockTicker()
        observeState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(batteryReceiver) }
        if (::overlayView.isInitialized && overlayView.isAttachedToWindow) {
            windowManager.removeView(overlayView)
        }
        HudRepository.setServiceRunning(false)
        super.onDestroy()
    }

    private fun addOverlayView(settings: HudSettings) {
        val params = buildLayoutParams(settings)
        layoutParams = params
        windowManager.addView(overlayView, params)
    }

    private fun buildLayoutParams(settings: HudSettings): WindowManager.LayoutParams {
        val margin = dp(16)
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = HudOverlayView.gravityFor(settings.position)
            x = margin
            y = margin
        }
    }

    private fun startClockTicker() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
        lifecycleScope.launch {
            while (isActive) {
                val now = Date()
                HudRepository.updateClock(timeFormat.format(now), dateFormat.format(now))
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            combine(settingsRepository.settings, HudRepository.state) { settings, runtime ->
                settings to runtime
            }.collect { (settings, runtime) ->
                val screenWidthPx = resources.displayMetrics.widthPixels
                overlayView.render(settings, runtime, screenWidthPx)

                val params = layoutParams
                if (params != null) {
                    params.gravity = HudOverlayView.gravityFor(settings.position)
                    windowManager.updateViewLayout(overlayView, params)
                }

                if (!settings.hudEnabled) {
                    stopSelf()
                }
            }
        }
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics).toInt()

    private fun buildNotification(): Notification {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AirHUD service",
                NotificationManager.IMPORTANCE_MIN
            )
            manager.createNotificationChannel(channel)
        }

        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AirHUD is running")
            .setContentText("Corner HUD is active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "airhud_service"
        private const val NOTIFICATION_ID = 42

        fun start(context: Context) {
            val intent = Intent(context, HudOverlayService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, HudOverlayService::class.java))
        }
    }
}
