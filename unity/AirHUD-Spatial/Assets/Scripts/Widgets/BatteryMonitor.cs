using System.Collections;
using AirHud.Data;
using UnityEngine;

namespace AirHud.Widgets
{
    /// <summary>
    /// Unity-side equivalent of the Android BatteryManager BroadcastReceiver.
    /// SystemInfo.batteryLevel/batteryStatus report real values on-device
    /// (Android/iOS); in the Editor they report -1/Unknown, which the view
    /// below handles gracefully.
    /// </summary>
    public class BatteryMonitor : MonoBehaviour
    {
        private void OnEnable() => StartCoroutine(Poll());

        private IEnumerator Poll()
        {
            var wait = new WaitForSeconds(5f);
            while (enabled)
            {
                float level = SystemInfo.batteryLevel;
                int percent = level >= 0f ? Mathf.RoundToInt(level * 100f) : -1;
                bool charging = SystemInfo.batteryStatus == BatteryStatus.Charging
                                || SystemInfo.batteryStatus == BatteryStatus.Full;
                HudRepository.UpdateBattery(percent, charging);
                yield return wait;
            }
        }
    }
}
