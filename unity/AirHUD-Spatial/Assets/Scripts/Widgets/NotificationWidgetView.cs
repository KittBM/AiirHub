using AirHud.Data;
using UnityEngine;
using UnityEngine.UI;

namespace AirHud.Widgets
{
    public class NotificationWidgetView : MonoBehaviour
    {
        [SerializeField] private GameObject root;
        [SerializeField] private Text appNameText;
        [SerializeField] private Text bodyText;

        private void OnEnable()
        {
            HudRepository.StateChanged += Render;
            Render(HudRepository.State);
        }

        private void OnDisable() => HudRepository.StateChanged -= Render;

        private void Render(HudRuntimeState state)
        {
            var notification = state.notification;
            if (root != null) root.SetActive(notification != null);
            if (notification == null) return;

            if (appNameText != null) appNameText.text = notification.appName;
            if (bodyText != null) bodyText.text = $"{notification.title}\n{notification.text}".Trim();
        }
    }
}
