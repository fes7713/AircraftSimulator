package aircraftsimulator.GameObject.Aircraft.FlightController;

import aircraftsimulator.GameObject.Aircraft.Aircraft;
import aircraftsimulator.GameObject.Aircraft.ForceApplier;
import aircraftsimulator.GameObject.DestructibleObject;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import java.util.List;

public class SimpleFlightController implements FlightControllerInterface {
    protected Aircraft parentObject;
    protected DestructibleObject target;
    private final float interval;
    private float intervalCount;
    private Vector3f waypoint;

    private float targetAngle;

    public static final float FLIGHT_CONTROLLER_INTERVAL = 1F;

    public SimpleFlightController(Aircraft parentObject, float interval){
        this.parentObject = parentObject;
        this.interval = interval;
        intervalCount = 0;
    }

    public SimpleFlightController(float interval)
    {
        this(null, interval);
    }

    public SimpleFlightController()
    {
        this(null, FLIGHT_CONTROLLER_INTERVAL);
    }

    @Override
    public Vector3f nextPoint(float delta) {
//        Vector3f point = new Vector3f(parentObject.getDirection());
//        point.scale(delta);
//        point.add(parentObject.getPosition());
        if(delta == 0)
            return waypoint;
        if(intervalCount <= 0)
        {
            intervalCount = interval;
            if(target == null)
                waypoint = null;
            else
                waypoint = getTargetPosition(delta);
        }

        intervalCount -= delta;
        return waypoint;
    }

    protected Vector3f getTargetPosition(float delta)
    {
        return new Vector3f(target.getPosition());
    }

    protected Vector3f getTargetVelocity(float delta){
        if(target instanceof Aircraft)
            return new Vector3f(((Aircraft)target).getVelocity());
        return new Vector3f(0, 0, 0);
    }

    @Override
    public Vector3f calculateLinearAcceleration(float delta) {
        Vector3f acceleration = new Vector3f(0, 0, 0);
        List<ForceApplier> forces = parentObject.getForceList();
        for(ForceApplier force: forces)
            acceleration.add(force.generateForce());
        return acceleration;
    }

    @Override
    public float calculateAngularAcceleration(float delta)
    {
        Vector3f waypointVector = new Vector3f(waypoint);
        waypointVector.sub(parentObject.getPosition());
        float angleDest = waypointVector.dot(parentObject.getDirection()) / waypointVector.length();
        float angleToStopAtMaxAngAcc =
                parentObject.getAngularSpeed() * parentObject.getAngularSpeed() / 2 / parentObject.getAngularAccelerationMagnitude();
        targetAngle = angleDest;
        float angularSpeed = parentObject.getAngularSpeed();
        float angularSpeedMax = parentObject.getAngularSpeedMax();
        float angularAccelerationMagnitude = parentObject.getAngularAccelerationMagnitude();
        float angularAcceleration;

        if(angleDest > 0.99F && parentObject.getAngularAcceleration() < 0)
            angularAcceleration = 0;
        else if(Math.cos(angleToStopAtMaxAngAcc) < angleDest)
        {
            // Check if speed goes to negative with acceleration wit max negative magnitude
            if(angularSpeed - angularAccelerationMagnitude * delta >= 0)
                angularAcceleration = -angularAccelerationMagnitude;
            else
                angularAcceleration = angularSpeed / delta ;
        }
        else
        {
            // Check if speed goes to negative with acceleration wit max positive magnitude
            if(angularSpeed + angularAccelerationMagnitude * delta <= angularSpeedMax)
                angularAcceleration = parentObject.getAngularAccelerationMagnitude();
            else
                angularAcceleration = (angularSpeedMax - angularSpeed) / delta;
        }

        return angularAcceleration;
    }

    // Rotation diff to the direction
    @Override
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

    @Override
    public float getTargetAngle() {
        return targetAngle;
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
