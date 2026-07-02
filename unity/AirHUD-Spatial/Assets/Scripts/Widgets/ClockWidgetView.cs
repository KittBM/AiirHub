using AirHud.Data;
using UnityEngine;
using UnityEngine.UI;

namespace AirHud.Widgets
{
    public class ClockWidgetView : MonoBehaviour
    {
        [SerializeField] private Text clockText;
        [SerializeField] private Text dateText;

        private void OnEnable()
        {
            HudRepository.StateChanged += Render;
            Render(HudRepository.State);
        }

        private void OnDisable() => HudRepository.StateChanged -= Render;

        private void Render(HudRuntimeState state)
        {
            if (clockText != null) clockText.text = state.clockText;
            if (dateText != null) dateText.text = state.dateText;
        }
    }
}
