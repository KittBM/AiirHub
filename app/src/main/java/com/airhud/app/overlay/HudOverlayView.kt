package com.airhud.app.overlay

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.airhud.app.data.HudRuntimeState
import com.airhud.app.data.HudSettings

/**
 * Plain-view (non-Compose) rendering of the corner HUD card. Runs inside a
 * WindowManager overlay window, which does not have a natural ViewTree
 * lifecycle/SavedStateRegistry owner, so we avoid hosting Compose here and
 * keep the widget stack as simple Views instead.
 */
class HudOverlayView(context: Context) : LinearLayout(context) {

    private val clockText = TextView(context).apply { setTextStyle(20f, Color.WHITE, bold = true) }
    private val dateText = TextView(context).apply { setTextStyle(13f, Color.LTGRAY) }
    private val batteryText = TextView(context).apply { setTextStyle(15f, Color.WHITE) }
    private val notificationApp = TextView(context).apply { setTextStyle(13f, ACCENT, bold = true) }
    private val notificationBody = TextView(context).apply {
        setTextStyle(14f, Color.WHITE)
        maxLines = 2
        ellipsize = android.text.TextUtils.TruncateAt.END
    }
    private val aiText = TextView(context).apply {
        setTextStyle(14f, Color.WHITE)
        maxLines = 4
        ellipsize = android.text.TextUtils.TruncateAt.END
    }

    private val notificationBlock = LinearLayout(context).apply {
        orientation = VERTICAL
        addView(notificationApp)
        addView(notificationBody)
    }

    init {
        orientation = VERTICAL
        val pad = dp(14f)
        setPadding(pad, pad, pad, pad)
        background = GradientDrawable().apply {
            cornerRadius = dp(16f).toFloat()
            setColor(Color.parseColor("#CC101418"))
        }
        addView(clockText)
        addView(dateText)
        addView(spacer())
        addView(batteryText)
        addView(spacer())
        addView(notificationBlock)
        addView(spacer())
        addView(aiText)
    }

    fun render(settings: HudSettings, state: HudRuntimeState, screenWidthPx: Int) {
        alpha = settings.opacityPercent.coerceIn(10, 100) / 100f

        layoutParams = (layoutParams ?: ViewGroup.LayoutParams(0, 0)).also {
            it.width = (screenWidthPx * settings.sizePercent.coerceIn(15, 60) / 100f).toInt()
            it.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }

        val widgets = settings.widgets

        clockText.isVisible = widgets.clockEnabled
        dateText.isVisible = widgets.clockEnabled
        clockText.text = state.clockText
        dateText.text = state.dateText

        batteryText.isVisible = widgets.batteryEnabled && state.batteryPercent >= 0
        batteryText.text = if (state.isCharging) {
            "⚡ Phone ${state.batteryPercent}%"
        } else {
            "🔋 Phone ${state.batteryPercent}%"
        }

        val notification = state.notification
        val showNotification = widgets.notificationEnabled && notification != null
        notificationBlock.isVisible = showNotification
        notificationApp.isVisible = showNotification
        notificationBody.isVisible = showNotification
        if (notification != null) {
            notificationApp.text = notification.appName
            notificationBody.text = "${notification.title}\n${notification.text}".trim()
        }

        val aiMessage = state.aiResponse ?: state.aiError
        aiText.isVisible = widgets.aiEnabled && (state.aiLoading || aiMessage != null)
        aiText.text = when {
            state.aiLoading -> "AI: ..."
            state.aiError != null -> "AI error: ${state.aiError}"
            else -> "AI: $aiMessage"
        }
    }

    private fun spacer(): View = View(context).apply {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(6f))
    }

    private fun TextView.setTextStyle(sizeSp: Float, color: Int, bold: Boolean = false) {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
        setTextColor(color)
        setTypeface(typeface, if (bold) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
        isVisible = false
    }

    private var View.isVisible: Boolean
        get() = visibility == View.VISIBLE
        set(value) {
            visibility = if (value) View.VISIBLE else View.GONE
        }

    private fun dp(value: Float): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics).toInt()

    companion object {
        private val ACCENT = Color.parseColor("#4FD1C5")

        fun gravityFor(position: com.airhud.app.data.HudPosition): Int = when (position) {
            com.airhud.app.data.HudPosition.TOP_RIGHT -> Gravity.TOP or Gravity.END
            com.airhud.app.data.HudPosition.TOP_LEFT -> Gravity.TOP or Gravity.START
            com.airhud.app.data.HudPosition.BOTTOM_RIGHT -> Gravity.BOTTOM or Gravity.END
            com.airhud.app.data.HudPosition.BOTTOM_LEFT -> Gravity.BOTTOM or Gravity.START
        }
    }
}
