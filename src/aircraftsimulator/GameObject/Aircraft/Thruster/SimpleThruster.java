package aircraftsimulator.GameObject.Aircraft.Thruster;

import aircraftsimulator.GameObject.Aircraft.Aircraft;
import aircraftsimulator.GameObject.Aircraft.ForceApplier;

import javax.vecmath.Vector3f;

public class SimpleThruster implements ForceApplier {
    private Aircraft aircraft;
    private float magnitude;

    public SimpleThruster(Aircraft aircraft, float magnitude)
    {
        this.aircraft = aircraft;
        this.magnitude = magnitude;
    }

    @Override
    public Vector3f generateForce() {
        Vector3f directionScaled = new Vector3f(aircraft.getDirection());
        directionScaled.normalize();
        directionScaled.scale(magnitude);
        return directionScaled ;
    }
}
