using AirHud.Data;
using UnityEngine;

namespace AirHud.Hud
{
    /// <summary>
    /// The "Widget Manager" from spec section 8.2: shows/hides each widget
    /// GameObject based on <see cref="HudSettings.widgets"/>. Ordering and
    /// notification-count limiting live in HudRepository (single latest
    /// notification, matching the MVP scope in section 11).
    /// </summary>
    public class WidgetManager : MonoBehaviour
    {
        [SerializeField] private GameObject clockWidget;
        [SerializeField] private GameObject batteryWidget;
        [SerializeField] private GameObject notificationWidget;
        [SerializeField] private GameObject aiWidget;

        private void OnEnable()
        {
            HudSettingsRepository.SettingsChanged += Refresh;
            Refresh(HudSettingsRepository.Current);
        }

        private void OnDisable()
        {
            HudSettingsRepository.SettingsChanged -= Refresh;
        }

        private void Refresh(HudSettings settings)
        {
            if (clockWidget != null) clockWidget.SetActive(settings.widgets.clockEnabled);
            if (batteryWidget != null) batteryWidget.SetActive(settings.widgets.batteryEnabled);
            if (notificationWidget != null) notificationWidget.SetActive(settings.widgets.notificationEnabled);
            if (aiWidget != null) aiWidget.SetActive(settings.widgets.aiEnabled);
        }
    }
}
