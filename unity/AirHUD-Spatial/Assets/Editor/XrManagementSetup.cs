using Unity.XR.XREAL;
using UnityEditor;
using UnityEditor.XR.Management;
using UnityEditor.XR.Management.Metadata;
using UnityEngine;
using UnityEngine.XR.Management;

namespace AirHud.EditorTools
{
    /// <summary>
    /// XREAL SDK 3.1.0 is a standard Unity XR Plugin Management provider (not a
    /// vendor-specific API), so "using" it is a Project Settings change: register
    /// XREALXRLoader as the active loader for the Android build target. Equivalent
    /// to manually checking XREAL in Project Settings > XR Plug-in Management >
    /// Android, but scriptable for headless/CI setup.
    /// </summary>
    public static class XrManagementSetup
    {
        [MenuItem("Tools/AirHUD/Enable XREAL Loader (Android)")]
        public static void EnableXrealForAndroid()
        {
            const BuildTargetGroup buildTargetGroup = BuildTargetGroup.Android;
            var generalSettings = GetOrCreateGeneralSettings();

            if (!generalSettings.HasSettingsForBuildTarget(buildTargetGroup))
            {
                generalSettings.CreateDefaultSettingsForBuildTarget(buildTargetGroup);
            }

            if (!generalSettings.HasManagerSettingsForBuildTarget(buildTargetGroup))
            {
                generalSettings.CreateDefaultManagerSettingsForBuildTarget(buildTargetGroup);
            }

            var managerSettings = generalSettings.ManagerSettingsForBuildTarget(buildTargetGroup);
            bool assigned = XRPackageMetadataStore.AssignLoader(
                managerSettings,
                typeof(XREALXRLoader).FullName,
                buildTargetGroup);

            AssetDatabase.SaveAssets();
            Debug.Log(assigned
                ? "[XrManagementSetup] XREAL loader enabled for Android."
                : "[XrManagementSetup] XREAL loader was already assigned (or assignment failed) for Android.");
        }

        private static XRGeneralSettingsPerBuildTarget GetOrCreateGeneralSettings()
        {
            EditorBuildSettings.TryGetConfigObject(
                XRGeneralSettings.k_SettingsKey,
                out XRGeneralSettingsPerBuildTarget generalSettings);

            if (generalSettings == null)
            {
                var assets = AssetDatabase.FindAssets("t:XRGeneralSettingsPerBuildTarget");
                if (assets.Length > 0)
                {
                    string existingPath = AssetDatabase.GUIDToAssetPath(assets[0]);
                    generalSettings = AssetDatabase.LoadAssetAtPath<XRGeneralSettingsPerBuildTarget>(existingPath);
                }
            }

            if (generalSettings != null) return generalSettings;

            generalSettings = ScriptableObject.CreateInstance<XRGeneralSettingsPerBuildTarget>();
            EnsureFolder("Assets/XR");
            EnsureFolder("Assets/XR/Settings");
            const string path = "Assets/XR/Settings/XRGeneralSettingsPerBuildTarget.asset";
            AssetDatabase.CreateAsset(generalSettings, path);
            AssetDatabase.SaveAssets();
            EditorBuildSettings.AddConfigObject(XRGeneralSettings.k_SettingsKey, generalSettings, true);
            return generalSettings;
        }

        private static void EnsureFolder(string path)
        {
            if (AssetDatabase.IsValidFolder(path)) return;
            int lastSlash = path.LastIndexOf('/');
            string parent = path.Substring(0, lastSlash);
            string folderName = path.Substring(lastSlash + 1);
            AssetDatabase.CreateFolder(parent, folderName);
        }
    }
}
