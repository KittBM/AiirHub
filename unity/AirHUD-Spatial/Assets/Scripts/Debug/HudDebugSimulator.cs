using AirHud.Ai;
using AirHud.Data;
using AirHud.Widgets;
using UnityEngine;

namespace AirHud.DebugTools
{
    /// <summary>
    /// Play-mode-only harness for exercising the HUD without a phone bridge or
    /// real XREAL hardware: enables the HUD on start and lets you trigger a test
    /// notification (N) or AI request (A) to see the widgets react.
    /// Not included in device builds.
    /// </summary>
    public class HudDebugSimulator : MonoBehaviour
    {
#if UNITY_EDITOR
        private void Start()
        {
            HudSettingsRepository.Update(s => s.hudEnabled = true);
            HudRepository.SetServiceRunning(true);
        }

        private void Update()
        {
            if (Input.GetKeyDown(KeyCode.N))
            {
                NotificationBridge.Push("LINE", "New message from Boss", "Where's the build?");
            }

            if (Input.GetKeyDown(KeyCode.A))
            {
                string prompt = "Say hello in five words.";
                HudRepository.SetAiPrompt(prompt);
                HudRepository.SetAiLoading();
                StartCoroutine(AiClient.Ask(
                    prompt,
                    HudSettingsRepository.Current.aiConfig,
                    onSuccess: r => HudRepository.SetAiResult(r),
                    onError: HudRepository.SetAiError));
            }

            if (Input.GetKeyDown(KeyCode.Alpha1)) HudSettingsRepository.Update(s => s.position = HudPosition.TopRight);
            if (Input.GetKeyDown(KeyCode.Alpha2)) HudSettingsRepository.Update(s => s.position = HudPosition.TopLeft);
            if (Input.GetKeyDown(KeyCode.Alpha3)) HudSettingsRepository.Update(s => s.position = HudPosition.BottomRight);
            if (Input.GetKeyDown(KeyCode.Alpha4)) HudSettingsRepository.Update(s => s.position = HudPosition.BottomLeft);
            if (Input.GetKeyDown(KeyCode.M)) HudSettingsRepository.Update(s => s.mode = (HudMode)(((int)s.mode + 1) % 3));
        }
#endif
    }
}
