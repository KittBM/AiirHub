using System.IO;
using UnityEditor;
using UnityEditor.Build.Reporting;
using UnityEngine;

namespace AirHud.EditorTools
{
    public static class AndroidBuildScript
    {
        private const string OutputPath = "Builds/Android/AirHUD-Spatial.apk";

        [MenuItem("Tools/AirHUD/Build Android APK")]
        public static void Build()
        {
            Directory.CreateDirectory("Builds/Android");

            var options = new BuildPlayerOptions
            {
                scenes = new[] { "Assets/Scenes/SpatialHUD.unity" },
                locationPathName = OutputPath,
                target = BuildTarget.Android,
                options = BuildOptions.None,
            };

            BuildReport report = BuildPipeline.BuildPlayer(options);
            BuildSummary summary = report.summary;

            Debug.Log("[AndroidBuildScript] Result="
                + $"{summary.result} Size={summary.totalSize} bytes "
                + $"Errors={summary.totalErrors} Warnings={summary.totalWarnings} "
                + $"Time={summary.totalTime}");

            if (summary.result != BuildResult.Succeeded)
            {
                EditorApplication.Exit(1);
            }
        }
    }
}
