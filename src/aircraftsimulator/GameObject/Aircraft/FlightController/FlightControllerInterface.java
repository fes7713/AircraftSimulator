package aircraftsimulator.GameObject.Aircraft.FlightController;

import aircraftsimulator.GameObject.Aircraft.Aircraft;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;

import javax.vecmath.Vector3f;

public interface FlightControllerInterface {
    Vector3f nextPoint(float delta);
    void setTarget(Information target);
    float getInterval();
    float getIntervalCount();
    void setParent(Aircraft parent);
    Vector3f rotatedDirection(float radian);
    float getTargetAngle();
    Vector3f calculateLinearAcceleration(float delta);
    float calculateAngularAcceleration(float delta);
    void configurationChanged();
}
