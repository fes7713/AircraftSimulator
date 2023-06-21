package aircraftsimulator.GameObject.Aircraft.FlightController;

import aircraftsimulator.GameObject.Aircraft.Aircraft;
import aircraftsimulator.GameObject.DestructibleObject;

import javax.vecmath.Vector3f;

public interface FlightControllerInterface {
    Vector3f nextPoint(float delta);
    void setTarget(DestructibleObject target);
    float getInterval();
    float getIntervalCount();
    void setParent(Aircraft parent);
    Vector3f rotatedDirection(float radian);
    float getTargetAngle();
    float calculateAngularAcceleration(float delta);
}
