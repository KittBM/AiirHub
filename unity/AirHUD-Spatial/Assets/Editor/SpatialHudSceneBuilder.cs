using AirHud.Ai;
using AirHud.DebugTools;
using AirHud.Hud;
using AirHud.Widgets;
using Unity.XR.CoreUtils;
using UnityEditor;
using UnityEditor.SceneManagement;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.SpatialTracking;
using UnityEngine.UI;

namespace AirHud.EditorTools
{
    /// <summary>
    /// Builds Assets/Scenes/SpatialHUD.unity from code instead of hand-authoring
    /// YAML. Run via Tools/AirHUD/Build Spatial HUD Scene, or headless with:
    ///   Unity.exe -batchmode -nographics -quit -projectPath &lt;path&gt;
    ///     -executeMethod AirHud.EditorTools.SpatialHudSceneBuilder.BuildScene
    /// </summary>
    public static class SpatialHudSceneBuilder
    {
        private const string ScenePath = "Assets/Scenes/SpatialHUD.unity";

        [MenuItem("Tools/AirHUD/Build Spatial HUD Scene")]
        public static void BuildScene()
        {
            var scene = EditorSceneManager.NewScene(NewSceneSetup.EmptyScene, NewSceneMode.Single);

            BuildCamera();
            var hudRoot = BuildHudRoot();
            var content = BuildContentContainer(hudRoot.transform);

            var clockWidget = BuildClockWidget(content);
            var batteryWidget = BuildBatteryWidget(content);
            var notificationWidget = BuildNotificationWidget(content);
            var aiWidget = BuildAiWidget(content);

            var widgetManager = hudRoot.AddComponent<WidgetManager>();
            AssignWidgetManagerRefs(widgetManager, clockWidget, batteryWidget, notificationWidget, aiWidget);

            var debugGo = new GameObject("DebugSimulator");
            debugGo.AddComponent<HudDebugSimulator>();

            EnsureScenesFolder();
            EditorSceneManager.SaveScene(scene, ScenePath);
            EditorBuildSettings.scenes = new[] { new EditorBuildSettingsScene(ScenePath, true) };
            Debug.Log($"[SpatialHudSceneBuilder] Saved {ScenePath}");
        }

        private static void EnsureScenesFolder()
        {
            if (!AssetDatabase.IsValidFolder("Assets/Scenes"))
            {
                AssetDatabase.CreateFolder("Assets", "Scenes");
            }
        }

        /// <summary>
        /// A standard XR Origin + TrackedPoseDriver rig, not a XREAL-specific camera
        /// class: XREAL SDK 3.1.0 is a Unity XR Plugin Management provider, so once
        /// XREAL is enabled as the active loader (Project Settings > XR Plug-in
        /// Management > Android), this Main Camera is driven by real head tracking
        /// automatically. HudRenderer keeps reading it via Camera.main / IHeadPoseProvider
        /// with no XREAL-specific code needed.
        /// </summary>
        private static void BuildCamera()
        {
            var originGo = new GameObject("XR Origin", typeof(XROrigin));
            var offsetGo = new GameObject("Camera Offset");
            offsetGo.transform.SetParent(originGo.transform, false);

            var camGo = new GameObject("Main Camera", typeof(Camera));
            camGo.tag = "MainCamera";
            camGo.transform.SetParent(offsetGo.transform, false);
            camGo.transform.SetPositionAndRotation(new Vector3(0f, 1.6f, 0f), Quaternion.identity);

            var cam = camGo.GetComponent<Camera>();
            cam.nearClipPlane = 0.05f;
            cam.fieldOfView = 52f; // Roughly matches XREAL Air's ~46° diagonal FOV class of device.

            var driver = camGo.AddComponent<TrackedPoseDriver>();
            driver.SetPoseSource(TrackedPoseDriver.DeviceType.GenericXRDevice, TrackedPoseDriver.TrackedPose.Center);
            driver.UseRelativeTransform = false;

            var origin = originGo.GetComponent<XROrigin>();
            origin.Camera = cam;
            origin.CameraFloorOffsetObject = offsetGo;
        }

        private static GameObject BuildHudRoot()
        {
            var go = new GameObject("HudRoot", typeof(RectTransform), typeof(Canvas), typeof(CanvasGroup));
            var canvas = go.GetComponent<Canvas>();
            canvas.renderMode = RenderMode.WorldSpace;

            var rect = go.GetComponent<RectTransform>();
            rect.sizeDelta = new Vector2(800, 500);

            var background = new GameObject("Background", typeof(RectTransform), typeof(Image));
            background.transform.SetParent(go.transform, false);
            var bgRect = background.GetComponent<RectTransform>();
            bgRect.anchorMin = Vector2.zero;
            bgRect.anchorMax = Vector2.one;
            bgRect.offsetMin = Vector2.zero;
            bgRect.offsetMax = Vector2.zero;
            background.GetComponent<Image>().color = new Color(0.063f, 0.078f, 0.094f, 0.8f);

            go.AddComponent<HudRenderer>();
            return go;
        }

        private static GameObject BuildContentContainer(Transform parent)
        {
            var go = new GameObject("Content", typeof(RectTransform), typeof(VerticalLayoutGroup));
            go.transform.SetParent(parent, false);
            var rect = go.GetComponent<RectTransform>();
            rect.anchorMin = Vector2.zero;
            rect.anchorMax = Vector2.one;
            rect.offsetMin = Vector2.zero;
            rect.offsetMax = Vector2.zero;

            var layout = go.GetComponent<VerticalLayoutGroup>();
            layout.padding = new RectOffset(28, 28, 24, 24);
            layout.spacing = 12;
            layout.childForceExpandHeight = false;
            layout.childForceExpandWidth = true;
            layout.childControlHeight = true;
            layout.childControlWidth = true;

            return go;
        }

        private static GameObject BuildClockWidget(GameObject parent)
        {
            var go = new GameObject("ClockWidget", typeof(RectTransform), typeof(VerticalLayoutGroup));
            go.transform.SetParent(parent.transform, false);
            var layout = go.GetComponent<VerticalLayoutGroup>();
            layout.childForceExpandWidth = true;
            layout.childControlWidth = true;
            layout.childControlHeight = true;

            var clockText = CreateText("ClockText", go.transform, 44, FontStyle.Bold, Color.white, TextAnchor.MiddleLeft);
            var dateText = CreateText("DateText", go.transform, 22, FontStyle.Normal, new Color(0.8f, 0.8f, 0.8f), TextAnchor.MiddleLeft);

            var view = go.AddComponent<ClockWidgetView>();
            AssignField(view, "clockText", clockText);
            AssignField(view, "dateText", dateText);
            go.AddComponent<ClockTicker>();
            return go;
        }

        private static GameObject BuildBatteryWidget(GameObject parent)
        {
            var go = new GameObject("BatteryWidget", typeof(RectTransform));
            go.transform.SetParent(parent.transform, false);
            var batteryText = CreateText("BatteryText", go.transform, 30, FontStyle.Normal, Color.white, TextAnchor.MiddleLeft);

            var view = go.AddComponent<BatteryWidgetView>();
            AssignField(view, "batteryText", batteryText);
            go.AddComponent<BatteryMonitor>();
            return go;
        }

        private static GameObject BuildNotificationWidget(GameObject parent)
        {
            var go = new GameObject("NotificationWidget", typeof(RectTransform), typeof(VerticalLayoutGroup));
            go.transform.SetParent(parent.transform, false);
            var layout = go.GetComponent<VerticalLayoutGroup>();
            layout.childForceExpandWidth = true;
            layout.childControlWidth = true;
            layout.childControlHeight = true;

            var appNameText = CreateText("AppNameText", go.transform, 26, FontStyle.Bold, new Color(0.31f, 0.82f, 0.77f), TextAnchor.MiddleLeft);
            var bodyText = CreateText("BodyText", go.transform, 28, FontStyle.Normal, Color.white, TextAnchor.UpperLeft);

            var view = go.AddComponent<NotificationWidgetView>();
            AssignField(view, "root", go);
            AssignField(view, "appNameText", appNameText);
            AssignField(view, "bodyText", bodyText);
            go.SetActive(false);
            return go;
        }

        private static GameObject BuildAiWidget(GameObject parent)
        {
            var go = new GameObject("AiWidget", typeof(RectTransform));
            go.transform.SetParent(parent.transform, false);
            var aiText = CreateText("AiText", go.transform, 26, FontStyle.Normal, Color.white, TextAnchor.UpperLeft);

            var view = go.AddComponent<AiWidgetView>();
            AssignField(view, "root", go);
            AssignField(view, "aiText", aiText);
            go.SetActive(false);
            return go;
        }

        private static Text CreateText(string name, Transform parent, int fontSize, FontStyle style, Color color, TextAnchor alignment)
        {
            var go = new GameObject(name, typeof(RectTransform), typeof(Text));
            go.transform.SetParent(parent, false);
            var text = go.GetComponent<Text>();
            text.font = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");
            text.fontSize = fontSize;
            text.fontStyle = style;
            text.color = color;
            text.alignment = alignment;
            text.horizontalOverflow = HorizontalWrapMode.Wrap;
            text.verticalOverflow = VerticalWrapMode.Overflow;

            var layoutElement = go.AddComponent<LayoutElement>();
            layoutElement.minHeight = fontSize * 1.3f;

            return text;
        }

        private static void AssignWidgetManagerRefs(WidgetManager manager, GameObject clock, GameObject battery, GameObject notification, GameObject ai)
        {
            AssignField(manager, "clockWidget", clock);
            AssignField(manager, "batteryWidget", battery);
            AssignField(manager, "notificationWidget", notification);
            AssignField(manager, "aiWidget", ai);
        }

        private static void AssignField(Object target, string fieldName, Object value)
        {
            var so = new SerializedObject(target);
            var prop = so.FindProperty(fieldName);
            if (prop == null)
            {
                Debug.LogError($"[SpatialHudSceneBuilder] Field '{fieldName}' not found on {target.GetType().Name}");
                return;
            }
            prop.objectReferenceValue = value;
            so.ApplyModifiedPropertiesWithoutUndo();
        }
    }
}
