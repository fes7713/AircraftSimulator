package aircraftsimulator.GameObject.Aircraft.FlightController.LostControl;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;

public interface LostControlInterface {
    void update(float delta);
    void setInformation(Information information);
    Information getTarget();
    float getLostTime();
    void disable();
    boolean isLost();
}
