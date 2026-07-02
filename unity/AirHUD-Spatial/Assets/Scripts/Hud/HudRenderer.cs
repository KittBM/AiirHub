using System.Collections;
using AirHud.Data;
using UnityEngine;

namespace AirHud.Hud
{
    /// <summary>
    /// The "Spatial Renderer / HUD Renderer" from spec sections 6 and 8.1: renders
    /// the panel, follows head pose, applies opacity/size, and animates show/hide.
    /// Positioning is corner-of-FOV rather than corner-of-screen since this runs in
    /// world space in front of an XR camera, not on a 2D canvas.
    /// </summary>
    [RequireComponent(typeof(CanvasGroup))]
    public class HudRenderer : MonoBehaviour
    {
        [Header("Placement")]
        [SerializeField] private float distanceMeters = 1.2f;
        [SerializeField] private float smoothFollowLerpSpeed = 4f;
        [SerializeField] private float fadeSeconds = 0.25f;

        [Header("Sizing")]
        [Tooltip("Converts the panel's pixel-space RectTransform (e.g. 800x500) into meters at 100% size.")]
        [SerializeField] private float baseScaleFactor = 0.0015f;

        [Header("Viewport anchors (x, y) per corner")]
        [SerializeField] private Vector2 topRightAnchor = new Vector2(0.82f, 0.80f);
        [SerializeField] private Vector2 topLeftAnchor = new Vector2(0.18f, 0.80f);
        [SerializeField] private Vector2 bottomRightAnchor = new Vector2(0.82f, 0.20f);
        [SerializeField] private Vector2 bottomLeftAnchor = new Vector2(0.18f, 0.20f);

        private RectTransform panelRoot;
        private CanvasGroup canvasGroup;
        private IHeadPoseProvider headPoseProvider;

        private bool worldLockCaptured;
        private Vector3 worldLockPosition;
        private Quaternion worldLockRotation;

        private bool lastHudEnabled;
        private Coroutine fadeRoutine;

        public void Init(IHeadPoseProvider provider)
        {
            headPoseProvider = provider;
        }

        private void Awake()
        {
            panelRoot = GetComponent<RectTransform>();
            canvasGroup = GetComponent<CanvasGroup>();
            headPoseProvider ??= new MainCameraHeadPoseProvider();
            canvasGroup.alpha = 0f;
        }

        private void OnEnable()
        {
            HudSettingsRepository.SettingsChanged += OnSettingsChanged;
            ApplyImmediate(HudSettingsRepository.Current);
        }

        private void OnDisable()
        {
            HudSettingsRepository.SettingsChanged -= OnSettingsChanged;
        }

        private void OnSettingsChanged(HudSettings settings)
        {
            if (settings.mode != HudMode.WorldLocked) worldLockCaptured = false;
            if (settings.hudEnabled != lastHudEnabled) AnimateVisibility(settings.hudEnabled);
        }

        private void ApplyImmediate(HudSettings settings)
        {
            lastHudEnabled = settings.hudEnabled;
            canvasGroup.alpha = settings.hudEnabled ? settings.opacityPercent / 100f : 0f;
        }

        private void AnimateVisibility(bool visible)
        {
            lastHudEnabled = visible;
            if (fadeRoutine != null) StopCoroutine(fadeRoutine);
            fadeRoutine = StartCoroutine(FadeTo(visible ? TargetAlpha() : 0f));
        }

        private float TargetAlpha() => HudSettingsRepository.Current.opacityPercent / 100f;

        private IEnumerator FadeTo(float target)
        {
            float start = canvasGroup.alpha;
            float t = 0f;
            while (t < fadeSeconds)
            {
                t += Time.deltaTime;
                canvasGroup.alpha = Mathf.Lerp(start, target, t / fadeSeconds);
                yield return null;
            }
            canvasGroup.alpha = target;
        }

        private void Update()
        {
            if (!headPoseProvider.IsAvailable) return;

            var settings = HudSettingsRepository.Current;
            if (!settings.hudEnabled) return;

            // Opacity can change live (slider) independent of the show/hide fade.
            if (fadeRoutine == null) canvasGroup.alpha = settings.opacityPercent / 100f;

            float scale = Mathf.Clamp(settings.sizePercent, 15, 60) / 100f;
            panelRoot.localScale = Vector3.one * (baseScaleFactor * scale);

            ComputeCornerPose(settings.position, out var targetPos, out var targetRot);

            switch (settings.mode)
            {
                case HudMode.HeadLocked:
                    panelRoot.SetPositionAndRotation(targetPos, targetRot);
                    break;

                case HudMode.SmoothFollow:
                    panelRoot.position = Vector3.Lerp(panelRoot.position, targetPos, Time.deltaTime * smoothFollowLerpSpeed);
                    panelRoot.rotation = Quaternion.Slerp(panelRoot.rotation, targetRot, Time.deltaTime * smoothFollowLerpSpeed);
                    break;

                case HudMode.WorldLocked:
                    if (!worldLockCaptured)
                    {
                        worldLockPosition = targetPos;
                        worldLockRotation = targetRot;
                        worldLockCaptured = true;
                    }
                    panelRoot.SetPositionAndRotation(worldLockPosition, worldLockRotation);
                    break;
            }
        }

        private void ComputeCornerPose(HudPosition position, out Vector3 worldPos, out Quaternion worldRot)
        {
            var cam = (headPoseProvider as MainCameraHeadPoseProvider)?.Camera ?? Camera.main;
            Vector2 anchor = position switch
            {
                HudPosition.TopRight => topRightAnchor,
                HudPosition.TopLeft => topLeftAnchor,
                HudPosition.BottomRight => bottomRightAnchor,
                HudPosition.BottomLeft => bottomLeftAnchor,
                _ => topRightAnchor,
            };

            if (cam != null)
            {
                worldPos = cam.ViewportToWorldPoint(new Vector3(anchor.x, anchor.y, distanceMeters));
            }
            else
            {
                Vector3 forward = headPoseProvider.Rotation * Vector3.forward;
                Vector3 right = headPoseProvider.Rotation * Vector3.right;
                Vector3 up = headPoseProvider.Rotation * Vector3.up;
                worldPos = headPoseProvider.Position + forward * distanceMeters
                           + right * (anchor.x - 0.5f) + up * (anchor.y - 0.5f);
            }

            worldRot = Quaternion.LookRotation(worldPos - headPoseProvider.Position, Vector3.up);
        }
    }
}
