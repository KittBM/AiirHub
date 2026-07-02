using System;

namespace AirHud.Data
{
    public enum HudPosition { TopRight, TopLeft, BottomRight, BottomLeft }

    public enum HudMode { HeadLocked, WorldLocked, SmoothFollow }

    public enum AiProvider { OpenAI, OpenRouter, Ollama, Custom }

    [Serializable]
    public class WidgetToggles
    {
        public bool clockEnabled = true;
        public bool batteryEnabled = true;
        public bool notificationEnabled = true;
        public bool aiEnabled = true;
    }

    [Serializable]
    public class AiConfig
    {
        public AiProvider provider = AiProvider.OpenAI;
        public string baseUrl = "https://api.openai.com/v1";
        public string apiKey = "";
        public string model = "gpt-4o-mini";
    }

    [Serializable]
    public class HudSettings
    {
        public bool hudEnabled = false;
        public HudPosition position = HudPosition.TopRight;
        public int sizePercent = 25;
        public int opacityPercent = 90;
        public HudMode mode = HudMode.HeadLocked;
        public WidgetToggles widgets = new WidgetToggles();
        public AiConfig aiConfig = new AiConfig();
    }

    [Serializable]
    public class NotificationInfo
    {
        public string appName;
        public string title;
        public string text;
        public long timestampMillis;
    }

    public class HudRuntimeState
    {
        public bool serviceRunning;
        public string clockText = "--:--";
        public string dateText = "";
        public int batteryPercent = -1;
        public bool isCharging;
        public NotificationInfo notification;
        public string aiPrompt = "";
        public string aiResponse;
        public bool aiLoading;
        public string aiError;
    }
}
