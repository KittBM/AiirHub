using System;
using UnityEngine;

namespace AirHud.Data
{
    /// <summary>
    /// Persists <see cref="HudSettings"/> via PlayerPrefs (the Unity-side equivalent
    /// of the Kotlin app's SharedPreferences-backed SettingsRepository) and notifies
    /// subscribers whenever settings change.
    /// </summary>
    public static class HudSettingsRepository
    {
        private const string PrefsKey = "airhud_settings_json";

        private static HudSettings current;
        public static HudSettings Current => current ??= Load();

        public static event Action<HudSettings> SettingsChanged;

        public static void Update(Action<HudSettings> mutate)
        {
            mutate(Current);
            Save(Current);
            SettingsChanged?.Invoke(Current);
        }

        private static HudSettings Load()
        {
            if (!PlayerPrefs.HasKey(PrefsKey)) return new HudSettings();
            try
            {
                return JsonUtility.FromJson<HudSettings>(PlayerPrefs.GetString(PrefsKey));
            }
            catch (Exception)
            {
                return new HudSettings();
            }
        }

        private static void Save(HudSettings settings)
        {
            PlayerPrefs.SetString(PrefsKey, JsonUtility.ToJson(settings));
            PlayerPrefs.Save();
        }
    }
}
