using AirHud.Data;

namespace AirHud.Widgets
{
    /// <summary>
    /// Entry point for phone notifications to reach the glasses renderer.
    /// Unity itself cannot read Android's NotificationListenerService — that data
    /// has to cross a process boundary from the Kotlin app (see the phone app's
    /// HudNotificationListenerService). Until that bridge (Unity-as-Library
    /// UnitySendMessage, or a local WebSocket per spec section 6's "HUD Data
    /// Layer") is wired up, call <see cref="Push"/> directly for testing.
    /// </summary>
    public static class NotificationBridge
    {
        public static void Push(string appName, string title, string text, float autoClearSeconds = 5f)
        {
            HudRepository.PostNotification(new NotificationInfo
            {
                appName = appName,
                title = title,
                text = text,
                timestampMillis = System.DateTimeOffset.UtcNow.ToUnixTimeMilliseconds(),
            }, autoClearSeconds);
        }
    }
}
