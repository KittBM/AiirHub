using AirHud.Data;
using UnityEngine;
using UnityEngine.UI;

namespace AirHud.Widgets
{
    /// <summary>
    /// Displays the AI assistant's answer on the HUD. The question itself is
    /// typed on the phone (spec 4.4) — this view is display-only, mirroring the
    /// "แสดงคำตอบบนแว่น" half of the flow.
    /// </summary>
    public class AiWidgetView : MonoBehaviour
    {
        [SerializeField] private GameObject root;
        [SerializeField] private Text aiText;
        [SerializeField] private int maxCharacters = 220;

        private void OnEnable()
        {
            HudRepository.StateChanged += Render;
            Render(HudRepository.State);
        }

        private void OnDisable() => HudRepository.StateChanged -= Render;

        private void Render(HudRuntimeState state)
        {
            string message = state.aiLoading ? "AI: ..." : state.aiError != null
                ? $"AI error: {state.aiError}"
                : state.aiResponse != null ? $"AI: {state.aiResponse}" : null;

            if (root != null) root.SetActive(message != null);
            if (message == null || aiText == null) return;

            aiText.text = message.Length > maxCharacters ? message.Substring(0, maxCharacters) + "…" : message;
        }
    }
}
