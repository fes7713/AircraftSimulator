package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.Aircraft.Thruster.Thruster;

import javax.vecmath.Vector3f;
import java.util.List;

public interface AircraftInterface {
    Vector3f getDirection();
    float getAngularSpeed();
    float getAngularAcceleration();
    float getAngularAccelerationMagnitude();
    float getAngularSpeedMax();
    List<ForceApplier> getForceList();
    void setThruster(Thruster thruster);
}
