package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.DestructibleObject;

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
        intervalCount = interval;
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
        if(intervalCount < 0)
        {
            intervalCount = interval;
            waypoint = new Vector3f(target.getPosition());
        }

        intervalCount -= delta;
        return waypoint;
    }

    public Vector3f nextDirection()
    {
        return new Vector3f(0, 0, 0);
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
