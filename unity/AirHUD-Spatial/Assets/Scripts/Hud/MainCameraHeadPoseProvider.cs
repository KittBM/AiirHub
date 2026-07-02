using UnityEngine;

namespace AirHud.Hud
{
    /// <summary>
    /// Default head pose source: Camera.main. Works unmodified in the Editor,
    /// on a desktop preview build, and once com.xreal.xr is imported and its
    /// camera rig sets itself as Camera.main (NRSDK/XREAL SDK both drive a
    /// standard Unity Camera under the hood for the center eye).
    /// </summary>
    public class MainCameraHeadPoseProvider : IHeadPoseProvider
    {
        private readonly Camera cam;

        public MainCameraHeadPoseProvider(Camera camera = null)
        {
            cam = camera != null ? camera : Camera.main;
        }

        public bool IsAvailable => cam != null;
        public Vector3 Position => cam != null ? cam.transform.position : Vector3.zero;
        public Quaternion Rotation => cam != null ? cam.transform.rotation : Quaternion.identity;

        public Camera Camera => cam;
    }
}
