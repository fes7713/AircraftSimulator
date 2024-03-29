package aircraftsimulator.GameObject.Aircraft.FlightController;

import aircraftsimulator.GameObject.Aircraft.Aircraft;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.MotionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformation;
import aircraftsimulator.GameObject.Aircraft.FlightController.LostControl.LastSeenControl;
import aircraftsimulator.GameObject.Aircraft.FlightController.LostControl.LostControlInterface;
import aircraftsimulator.GameObject.Aircraft.FlightController.LostControl.PredictionControl;
import aircraftsimulator.GameObject.DestructibleObjectInterface;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import java.awt.*;

import static aircraftsimulator.GameObject.Aircraft.Aircraft.FLIGHT_CONTROLLER_INTERVAL;

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

    private LostControlInterface lostControl;

    public SimpleFlightController(Aircraft parentObject, float interval){
        this.parentObject = parentObject;
        this.interval = interval;
        intervalCount = 0;
        lostControl = new PredictionControl();
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
    public void update(float delta) {
        lostControl.update(delta);
        if(intervalCount <= 0) {
            intervalCount = interval;
            nextPoint(delta);
        }

        intervalCount -= delta;
    }

    @Override
    public void nextPoint(float delta) {
        if(parentObject.getAngularSpeed() <= 0 && (lostControl.isLost() || target == null)) {
            waypoint = null;
            lostControl.disable();
        }
        else {
            if(lostControl.isLost())
            {
                target = lostControl.getTarget();
            }
            Vector3f a = getTargetFuturePosition(delta, parentObject.getPosition(), parentObject.getVelocity());
            if (a != null)
                waypoint = a;
        }
    }

    protected Vector3f getTargetFuturePosition(float delta, Vector3f position, Vector3f velocity)
    {
        return getTargetPosition();
    }

    protected Vector3f getTargetPosition()
    {
        if(target instanceof PositionInformation info)
            return new Vector3f(info.getPosition());
        return null;
    }

    protected Vector3f getTargetVelocity(){
        if(target instanceof MotionInformation info)
            return new Vector3f(info.getVelocity());
        return null;
    }

    @Override
    public void calculateLinearAcceleration(float delta) {

    }

    @Override
    public float calculateAngularAcceleration(float delta)
    {
        float angularAcceleration;
        float angularSpeed = parentObject.getAngularSpeed();
        final float angularAccelerationMagnitude = parentObject.getAngularAccelerationMagnitude();
        if((target == null && !lostControl.isLost()) || waypoint == null)
        {
            if(angularSpeed - angularAccelerationMagnitude * delta >= 0)
                angularAcceleration = -angularAccelerationMagnitude;
            else if(angularSpeed <= 0)
                angularAcceleration = 0;
            else
                angularAcceleration = - angularSpeed / delta ;
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
        if(angleDest > 0.9999F && parentObject.getAngularSpeed() <= 0)
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
        else if(Math.cos(angularSpeed / 2 / angularSpeed * delta) < angleDest)
        {
            angularAcceleration =  - angularSpeed / delta;
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
    public void setTarget(@Nullable Information target) {
        if(this.target != null && target == null)
        {
            if(this.target.getSource() instanceof DestructibleObjectInterface o && !o.isAlive())
                lostControl.setInformation(null);
            else
            {
                if(this.target.getSource() instanceof Aircraft)
                    lostControl = new LastSeenControl();
                else
                    lostControl = new PredictionControl();

                lostControl.setInformation(this.target);
            }
        }
        if(this.target != null && target != null)
            lostControl.disable();
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

    @Override
    public void draw(Graphics2D g2d) {

        float size = parentObject.getSize();
        if(target != null && target instanceof PositionInformation)
        {
            Vector3f p = ((PositionInformation)target).getPosition();
            g2d.drawLine((int)(p.x - size), (int)(p.y), (int)(p.x + size), (int)(p.y));
            g2d.drawLine((int)(p.x), (int)(p.y - size), (int)(p.x), (int)(p.y + size));
        }

        if(waypoint != null)
        {
            g2d.drawLine((int)(waypoint.x - size), (int)(waypoint.y - size), (int)(waypoint.x + size), (int)(waypoint.y + size));
            g2d.drawLine((int)(waypoint.x + size), (int)(waypoint.y - size), (int)(waypoint.x - size), (int)(waypoint.y + size));
        }
    }

    @Override
    public Information getInformation() {
        return target;
    }
}
