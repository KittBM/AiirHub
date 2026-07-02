using UnityEngine;

namespace AirHud.Hud
{
    /// <summary>
    /// Abstracts "where is the user's head/eyes" so HudRenderer never talks to a
    /// concrete camera or XR rig directly. Swap <see cref="MainCameraHeadPoseProvider"/>
    /// for an XREAL SDK-backed implementation (e.g. the SDK's center-eye anchor) once
    /// com.xreal.xr is imported, without touching HudRenderer itself.
    /// </summary>
    public interface IHeadPoseProvider
    {
        Vector3 Position { get; }
        Quaternion Rotation { get; }
        bool IsAvailable { get; }
    }
}
