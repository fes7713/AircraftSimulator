package aircraftsimulator.GameObject.Aircraft.FlightController;

import aircraftsimulator.GameObject.Aircraft.Aircraft;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.MotionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

public class SimpleFlightController implements FlightControllerInterface {
    @NotNull
    protected Aircraft parentObject;
    @Nullable
    protected Information target;
    @Nullable
    protected Vector3f waypoint;

    private final float interval;
    private float intervalCount;
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
            if(target == null && parentObject.getAngularSpeed() <= 0)
                waypoint = null;
            else
                waypoint = getTargetFuturePosition(delta, parentObject.getPosition(), parentObject.getVelocity());
        }

        intervalCount -= delta;
        return waypoint;
    }

    protected float timeToPoint(Vector3f point)
    {
        point.sub(parentObject.getPosition());

        return (float)Math.sqrt(point.lengthSquared() / parentObject.getVelocity().lengthSquared());
    }

    protected Vector3f getTargetFuturePosition(float delta, Vector3f position, Vector3f velocity)
    {
        return getTargetPosition(delta);
    }

    protected Vector3f getTargetPosition(float delta)
    {
        if(target instanceof MotionInformation)
            return new Vector3f(((PositionInformation)target).getPosition());
        return new Vector3f(0, 0, 0);
    }

    protected Vector3f getTargetVelocity(float delta){
        if(target instanceof MotionInformation)
            return new Vector3f(((MotionInformation)target).getVelocity());
        return new Vector3f(0, 0, 0);
    }

    @Override
    public Vector3f calculateLinearAcceleration(float delta) {
        return parentObject.getAcceleration();
    }

    @Override
    public float calculateAngularAcceleration(float delta)
    {
        float angularAcceleration;
        float angularSpeed = parentObject.getAngularSpeed();
        final float angularAccelerationMagnitude = parentObject.getAngularAccelerationMagnitude();
        if(waypoint == null)
        {
            if(angularSpeed - angularAccelerationMagnitude * delta >= 0)
                angularAcceleration = -angularAccelerationMagnitude;
            else
                angularAcceleration = angularSpeed / delta ;
            return angularAcceleration;
        }
        Vector3f waypointVector = new Vector3f(waypoint);
        waypointVector.sub(parentObject.getPosition());
        float angleDest = waypointVector.dot(parentObject.getDirection()) / waypointVector.length();
        float angleToStopAtMaxAngAcc =
                angularSpeed * angularSpeed / 2 / angularAccelerationMagnitude;
        targetAngle = angleDest;

        float angularSpeedMax = parentObject.getAngularSpeedMax();


        // Finished turning so angular acceleration is zero
        if(angleDest > 0.99F && parentObject.getAngularAcceleration() < 0)
            angularAcceleration = 0;
        // Not yet hit the angular deceleration overhead zone where I need to decelerate in advance
        else if(Math.cos(angleToStopAtMaxAngAcc) < angleDest)
        {
            // Check if angular speed goes to negative with acceleration wit max negative magnitude
            if(angularSpeed - angularAccelerationMagnitude * delta >= 0)
                angularAcceleration = - angularAccelerationMagnitude;
            else
                angularAcceleration =  - angularSpeed / delta ;
        }
        else
        {
            // Check if speed goes to negative with acceleration wit max positive magnitude
            if(angularSpeed + angularAccelerationMagnitude * delta <= angularSpeedMax)
                angularAcceleration = angularAccelerationMagnitude;
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
            n.set(0, 0, 1);
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
    public void setTarget(Information target) {
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

    @Override
    public void configurationChanged() {

    }
}
