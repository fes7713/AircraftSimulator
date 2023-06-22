package aircraftsimulator.GameObject.Aircraft.Thruster;

import aircraftsimulator.GameObject.Aircraft.Aircraft;

import javax.vecmath.Vector3f;

public class SimpleThruster extends Thruster {
    private final Aircraft aircraft;
    protected float magnitude;

    public SimpleThruster(Aircraft aircraft, float magnitude)
    {
        this.aircraft = aircraft;
        this.magnitude = magnitude;
    }

    protected Vector3f normalizedForce()
    {
        Vector3f normalizedForce = new Vector3f(aircraft.getDirection());
        normalizedForce.normalize();
        return normalizedForce;
    }

    @Override
    public Vector3f generateForce() {
        Vector3f force = normalizedForce();
        force.scale(magnitude);
        return force ;
    }
}
