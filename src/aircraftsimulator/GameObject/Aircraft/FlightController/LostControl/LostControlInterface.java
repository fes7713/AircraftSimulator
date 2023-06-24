package aircraftsimulator.GameObject.Aircraft.FlightController.LostControl;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;

import javax.vecmath.Vector3f;

public interface LostControlInterface {
    void update(float delta);
    void setInformation(Information information);
    Vector3f getPosition();
    Vector3f getVelocity();
    float getLostTime();
    void disable();
    boolean isLost();
}
