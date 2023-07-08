package aircraftsimulator.GameObject.Aircraft;

import javax.vecmath.Vector3f;

public class AirResistance implements ForceApplier{

    private final MovingObjectInterface movingObject;
    private float coefficient;
    public AirResistance(MovingObjectInterface movingObject, float coefficient)
    {
        this.movingObject = movingObject;
        this.coefficient = coefficient;
    }

    @Override
    public Vector3f generateForce() {
        Vector3f force = new Vector3f(movingObject.getVelocity());
        force.negate();
        force.normalize();
        force.scale(coefficient * movingObject.getVelocity().lengthSquared());
        return force;
    }

    public float getCoefficient()
    {
        return coefficient;
    }

    public void setCoefficient(float value)
    {
        coefficient = value;
    }

    public static float MaxSpeedForForce(AirResistance airResistance, float force)
    {
        return (float) Math.sqrt(force / airResistance.getCoefficient());
    }
}
