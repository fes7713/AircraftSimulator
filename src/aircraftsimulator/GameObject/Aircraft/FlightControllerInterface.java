package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.DestructibleObject;

import javax.vecmath.Vector3f;

public interface FlightControllerInterface {
    Vector3f nextPoint(float delta);
    void setTarget(DestructibleObject target);
    float getInterval();
    float getIntervalCount();
    void setParent(Aircraft parent);
}
