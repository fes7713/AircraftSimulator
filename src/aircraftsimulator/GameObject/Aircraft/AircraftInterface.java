package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.Aircraft.Communication.Network;
import aircraftsimulator.GameObject.Aircraft.Thruster.Thruster;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.DestructibleObjectInterface;

public interface AircraftInterface extends DestructibleObjectInterface {
    float getAngularSpeed();
    float getAngularAcceleration();
    float getAngularAccelerationMagnitude();
    float getAngularSpeedMax();
    void setThruster(Thruster thruster);
    void addComponent(Component component);
    void removeComponent(Component component);
    Network getNetwork();
}
