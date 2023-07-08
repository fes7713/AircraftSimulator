package aircraftsimulator.GameObject.Aircraft.Thruster;

import aircraftsimulator.GameObject.Aircraft.Aircraft;

import javax.vecmath.Vector3f;

public class SimpleThruster extends Thruster {
    protected float magnitude;

    public SimpleThruster(Aircraft parent, float fuel, float magnitude)
    {
        super(parent, fuel);
        this.magnitude = magnitude;
    }

    protected Vector3f normalizedForce()
    {
        Vector3f normalizedForce = new Vector3f(parent.getDirection());
        normalizedForce.normalize();
        return normalizedForce;
    }

    @Override
    public Vector3f generateForce() {
        if(fuel <= 0)
            return new Vector3f(0, 0, 0);
        Vector3f force = normalizedForce();
        force.scale(magnitude);
        return force ;
    }

    @Override
    public float getMagnitude() {
        return magnitude;
    }
}
