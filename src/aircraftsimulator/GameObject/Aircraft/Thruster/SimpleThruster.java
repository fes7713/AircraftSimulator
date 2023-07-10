package aircraftsimulator.GameObject.Aircraft.Thruster;

import aircraftsimulator.GameObject.Aircraft.Aircraft;

import javax.vecmath.Vector3f;

public class SimpleThruster extends Thruster {

    public SimpleThruster(Aircraft parent, float magnitude, float fuel)
    {
        super(parent, magnitude, fuel);
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
}
