package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.DestructibleObject;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

public class SimpleFlightController implements FlightControllerInterface {
    private Aircraft parentObject;
    private DestructibleObject target;
    private final float interval;
    private float intervalCount;
    private Vector3f waypoint;

    public SimpleFlightController(Aircraft parentObject, float interval){
        this.parentObject = parentObject;
        this.interval = interval;
        intervalCount = 0;
    }

    public SimpleFlightController(float interval)
    {
        this(null, interval);
    }

    @Override
    public Vector3f nextPoint(float delta) {
//        Vector3f point = new Vector3f(parentObject.getDirection());
//        point.scale(delta);
//        point.add(parentObject.getPosition());
        if(intervalCount <= 0)
        {
            intervalCount = interval;
            if(target == null)
                waypoint = null;
            else
                waypoint = new Vector3f(target.getPosition());
        }

        intervalCount -= delta;
        return waypoint;
    }

    // Rotation diff to the direction
    public Vector3f rotatedDirection(float radian)
    {
        if(waypoint == null)
            return new Vector3f(0, 0, 0);
        Vector3f direction = parentObject.getDirection();
        Vector3f destinationVector = new Vector3f(waypoint);
        destinationVector.sub(parentObject.getPosition());
        Vector3f n = new Vector3f();
        n.cross(direction, destinationVector);
        if(n.lengthSquared() == 0)
            n.set(1, 0, 0);
        n.normalize();
        Matrix3f rotationMatrix = new Matrix3f(
                n.x * n.x, n.x * n.y, n.x * n.z,
                n.y * n.x, n.y * n.y, n.y * n.z,
                n.z * n.x, n.z * n.y, n.z * n.z);
        rotationMatrix.mul((float)(1 - Math.cos(radian)));
        rotationMatrix.add(new Matrix3f(
                (float)Math.cos(radian), (float)(- n.z * Math.sin(radian)), (float)(n.y * Math.sin(radian)),
                (float)(n.z * Math.sin(radian)), (float)Math.cos(radian), (float)(-n.x * Math.sin(radian)),
                (float)(- n.y * Math.sin(radian)), (float)(n.x * Math.sin(radian)), (float)Math.cos(radian))
        );
        Vector3f r = new Vector3f();
        rotationMatrix.transform(direction, r);
        r.normalize();
        return r;
    }

    public Vector3f nextDirection()
    {
        return parentObject.getDirection();
    }

    @Override
    public void setTarget(DestructibleObject target) {
        this.target = target;
    }

    @Override
    public float getInterval() {
        return interval;
    }

    @Override
    public float getIntervalCount() {
        return intervalCount;
    }

    @Override
    public void setParent(Aircraft parent) {
        parentObject = parent;
    }
}
