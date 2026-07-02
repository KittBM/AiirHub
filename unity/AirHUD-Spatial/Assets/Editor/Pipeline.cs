using UnityEditor;

namespace AirHud.EditorTools
{
    /// <summary>
    /// Single entry point chaining scene build -> player settings -> Android
    /// build, so CI/headless runs only pay Editor startup cost once.
    /// </summary>
    public static class Pipeline
    {
        [MenuItem("Tools/AirHUD/Full Pipeline (Scene + Settings + Build)")]
        public static void Run()
        {
            SpatialHudSceneBuilder.BuildScene();
            XrManagementSetup.EnableXrealForAndroid();
            PlayerSettingsSetup.Apply();
            AndroidBuildScript.Build();
        }
    }
}
