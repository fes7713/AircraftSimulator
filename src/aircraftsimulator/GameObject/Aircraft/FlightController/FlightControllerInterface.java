package aircraftsimulator.GameObject.Aircraft.FlightController;

import aircraftsimulator.GameObject.Aircraft.Aircraft;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;

import javax.vecmath.Vector3f;
import java.awt.*;

public interface FlightControllerInterface {
    void update(float delta);
    void nextPoint(float delta);
    void setTarget(Information target);
    float getInterval();
    float getIntervalCount();
    void setParent(Aircraft parent);
    Vector3f rotatedDirection(float radian);
    float getTargetAngle();
    Vector3f calculateLinearAcceleration(float delta);
    float calculateAngularAcceleration(float delta);
    void configurationChanged();
    Vector3f getWaypoint();
    void draw(Graphics2D g2d);
}
