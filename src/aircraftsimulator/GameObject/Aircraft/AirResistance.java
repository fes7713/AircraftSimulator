package aircraftsimulator.GameObject.Aircraft;

import javax.vecmath.Vector3f;

public class AirResistance implements ForceApplier{

    private Aircraft aircraft;
    private float coefficient;
    public AirResistance(Aircraft aircraft, float coefficient)
    {
        this.aircraft = aircraft;
        this.coefficient = coefficient;
    }

    @Override
    public Vector3f generateForce() {
        Vector3f force = new Vector3f(aircraft.getVelocity());
        force.negate();
        force.normalize();
        force.scale(coefficient * aircraft.getVelocity().lengthSquared());
        return force;
    }
}
