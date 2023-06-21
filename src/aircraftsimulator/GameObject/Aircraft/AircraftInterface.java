package aircraftsimulator.GameObject.Aircraft;

import javax.vecmath.Vector3f;

public interface AircraftInterface {
    Vector3f getDirection();
    float getAngularSpeed();
    float getAngularAcceleration();
    float getAngularAccelerationMagnitude();
    float getAngularSpeedMax();
}
