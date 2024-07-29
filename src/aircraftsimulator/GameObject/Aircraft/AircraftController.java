package aircraftsimulator.GameObject.Aircraft;

import javax.vecmath.Vector3f;

public interface AircraftController {
    void setAngularAcceleration(float acceleration);
    void setAngularSpeed(float speed);
    void setDirection(Vector3f direction);
}
