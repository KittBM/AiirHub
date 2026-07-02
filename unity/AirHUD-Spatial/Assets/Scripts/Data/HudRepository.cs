using System;
using System.Collections;
using UnityEngine;

namespace AirHud.Data
{
    /// <summary>
    /// Runtime "HUD Data Layer / State Stream" from spec section 6. Every widget
    /// reads from this single state object and reacts to <see cref="StateChanged"/>;
    /// on the phone side (Kotlin) the analogous type is HudRepository/StateFlow.
    /// Once the Android app is bridged to this Unity renderer, its data should be
    /// pushed in here instead of the mock producers under Widgets/.
    /// </summary>
    public static class HudRepository
    {
        public static HudRuntimeState State { get; } = new HudRuntimeState();
        public static event Action<HudRuntimeState> StateChanged;

        private static Runner runner;
        private static Coroutine notificationClearRoutine;
        private static Coroutine aiClearRoutine;

        [RuntimeInitializeOnLoadMethod(RuntimeInitializeLoadType.BeforeSceneLoad)]
        private static void EnsureRunner()
        {
            if (runner != null) return;
            var go = new GameObject("HudRepositoryRunner");
            UnityEngine.Object.DontDestroyOnLoad(go);
            runner = go.AddComponent<Runner>();
        }

        private static void Notify() => StateChanged?.Invoke(State);

        public static void SetServiceRunning(bool running)
        {
            State.serviceRunning = running;
            Notify();
        }

        public static void UpdateClock(string clockText, string dateText)
        {
            State.clockText = clockText;
            State.dateText = dateText;
            Notify();
        }

        public static void UpdateBattery(int percent, bool isCharging)
        {
            State.batteryPercent = percent;
            State.isCharging = isCharging;
            Notify();
        }

        public static void PostNotification(NotificationInfo info, float autoClearSeconds = 5f)
        {
            EnsureRunner();
            if (notificationClearRoutine != null) runner.StopCoroutine(notificationClearRoutine);
            State.notification = info;
            Notify();
            notificationClearRoutine = runner.StartCoroutine(ClearNotificationAfter(info, autoClearSeconds));
        }

        private static IEnumerator ClearNotificationAfter(NotificationInfo info, float seconds)
        {
            yield return new WaitForSeconds(seconds);
            if (State.notification == info)
            {
                State.notification = null;
                Notify();
            }
        }

        public static void SetAiPrompt(string prompt)
        {
            State.aiPrompt = prompt;
            Notify();
        }

        public static void SetAiLoading()
        {
            EnsureRunner();
            if (aiClearRoutine != null) runner.StopCoroutine(aiClearRoutine);
            State.aiLoading = true;
            State.aiError = null;
            Notify();
        }

        public static void SetAiResult(string response, float autoClearSeconds = 20f)
        {
            EnsureRunner();
            State.aiLoading = false;
            State.aiResponse = response;
            State.aiError = null;
            Notify();
            if (aiClearRoutine != null) runner.StopCoroutine(aiClearRoutine);
            aiClearRoutine = runner.StartCoroutine(ClearAiAfter(seconds: autoClearSeconds));
        }

        public static void SetAiError(string message)
        {
            State.aiLoading = false;
            State.aiError = message;
            Notify();
        }

        private static IEnumerator ClearAiAfter(float seconds)
        {
            yield return new WaitForSeconds(seconds);
            State.aiResponse = null;
            Notify();
        }

        private class Runner : MonoBehaviour { }
    }
}
