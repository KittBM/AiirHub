using UnityEditor;
using UnityEditor.Build;
using UnityEngine;

namespace AirHud.EditorTools
{
    /// <summary>
    /// Applies the Android Player Settings XREAL SDK requires (min SDK 31+,
    /// IL2CPP, ARM64) — see docs.xreal.com "Getting Started with XREAL SDK".
    /// </summary>
    public static class PlayerSettingsSetup
    {
        [MenuItem("Tools/AirHUD/Apply XREAL Player Settings")]
        public static void Apply()
        {
            PlayerSettings.companyName = "AirHUD";
            PlayerSettings.productName = "AirHUD Spatial";

            PlayerSettings.SetApplicationIdentifier(NamedBuildTarget.Android, "com.airhud.spatial");
            // Unity 2022.3.0f1's AndroidSdkVersions enum only has named constants up to
            // API 30; XREAL requires API 31+, so set it via the underlying int value.
            PlayerSettings.Android.minSdkVersion = (AndroidSdkVersions)31;
            PlayerSettings.Android.targetSdkVersion = AndroidSdkVersions.AndroidApiLevelAuto;
            PlayerSettings.Android.targetArchitectures = AndroidArchitecture.ARM64;
            PlayerSettings.SetScriptingBackend(NamedBuildTarget.Android, ScriptingImplementation.IL2CPP);

            AssetDatabase.SaveAssets();
            Debug.Log("[PlayerSettingsSetup] Applied XREAL-required Android player settings "
                + $"(minSdk={PlayerSettings.Android.minSdkVersion}, "
                + $"scriptingBackend={PlayerSettings.GetScriptingBackend(NamedBuildTarget.Android)}, "
                + $"arch={PlayerSettings.Android.targetArchitectures}).");
        }
    }
}
