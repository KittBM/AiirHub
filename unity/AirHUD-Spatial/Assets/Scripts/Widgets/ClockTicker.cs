using System;
using System.Collections;
using AirHud.Data;
using UnityEngine;

namespace AirHud.Widgets
{
    /// <summary>
    /// Unity-side equivalent of HudOverlayService's clock coroutine on Android:
    /// ticks once per second and pushes into the shared HudRepository.
    /// </summary>
    public class ClockTicker : MonoBehaviour
    {
        private void OnEnable() => StartCoroutine(Tick());

        private IEnumerator Tick()
        {
            var wait = new WaitForSeconds(1f);
            while (enabled)
            {
                var now = DateTime.Now;
                HudRepository.UpdateClock(now.ToString("HH:mm"), now.ToString("ddd, d MMM"));
                yield return wait;
            }
        }
    }
}
