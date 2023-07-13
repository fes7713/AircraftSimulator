package aircraftsimulator.GameObject.Aircraft.Communication.Information;

import javax.vecmath.Vector3f;

public interface MotionInformation extends PositionInformation{
    Vector3f getVelocity();
    Vector3f getAcceleration();
    Vector3f getDirection();
}
