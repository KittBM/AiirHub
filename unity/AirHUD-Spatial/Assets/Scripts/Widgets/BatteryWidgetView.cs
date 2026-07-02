using AirHud.Data;
using UnityEngine;
using UnityEngine.UI;

namespace AirHud.Widgets
{
    public class BatteryWidgetView : MonoBehaviour
    {
        [SerializeField] private Text batteryText;

        private void OnEnable()
        {
            HudRepository.StateChanged += Render;
            Render(HudRepository.State);
        }

        private void OnDisable() => HudRepository.StateChanged -= Render;

        private void Render(HudRuntimeState state)
        {
            if (batteryText == null) return;
            batteryText.text = state.batteryPercent >= 0
                ? $"{(state.isCharging ? "⚡" : "🔋")} Phone {state.batteryPercent}%"
                : "Phone --%";
        }
    }
}
