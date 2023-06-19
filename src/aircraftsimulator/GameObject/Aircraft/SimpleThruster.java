package aircraftsimulator.GameObject.Aircraft;

import javax.vecmath.Vector3f;

public class SimpleThruster implements ForceApplier{
    private float magnitude;

    public SimpleThruster(float magnitude)
    {
        this.magnitude = magnitude;
    }

    @Override
    public Vector3f generateForce(Vector3f direction) {
        Vector3f directionScaled = new Vector3f(direction);
        directionScaled.scale(magnitude);
        return directionScaled ;
    }
}
