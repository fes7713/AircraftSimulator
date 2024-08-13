package aircraftsimulator.GameObject.Aircraft.FlightController.v2;

import aircraftsimulator.GameMath;
import aircraftsimulator.GameObject.Aircraft.Aircraft;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Network;
import aircraftsimulator.GameObject.Aircraft.Communication.NetworkComponent;
import aircraftsimulator.GameObject.Aircraft.Communication.SlowStartApplicationNetworkComponentImp;
import aircraftsimulator.GameObject.Aircraft.FlightController.PositionData;
import aircraftsimulator.GameObject.Aircraft.SystemPort;
import aircraftsimulator.GameObject.GameObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Vector3f;
import java.awt.*;

public class SimpleFlightController extends FlightControlV2 {
    @NotNull
    protected Aircraft parent;

    @Nullable
    protected Vector3f target;
    private float targetAngle;

    private final NetworkComponent networkComponent;

    public SimpleFlightController(Aircraft parent, Network network){
        this.parent = parent;

        networkComponent = new SlowStartApplicationNetworkComponentImp(network);
        networkComponent.openPort(SystemPort.FLIGHT_CONTROL);
        networkComponent.enabledPortTransfer(SystemPort.FLIGHT_CONTROL);

        networkComponent.addDataReceiver(PositionData.class, (data, port) -> {
            setTarget(data.position());
        });
    }

    public void update(float delta) {
        parent.setAngularAcceleration(calculateAngularAcceleration(delta));

        float angle = parent.getAngularSpeed() * delta;

        if(angle != 0) {
            Vector3f directionNew = rotatedDirection(angle);
            parent.setDirection(directionNew);
        }
    }

    public float calculateAngularAcceleration(float delta)
    {
        float angularAcceleration;
        float angularSpeed = parent.getAngularSpeed();
        final float angularAccelerationMagnitude = parent.getAngularAccelerationMagnitude();
        if(target == null)
        {
            if(angularSpeed - angularAccelerationMagnitude * delta >= 0)
                angularAcceleration = -angularAccelerationMagnitude;
            else if(angularSpeed <= 0)
                angularAcceleration = 0;
            else
                angularAcceleration = - angularSpeed / delta ;
            return angularAcceleration;
        }
        Vector3f diff = new Vector3f(target);
        diff.sub(parent.getPosition());
        float angleDest = diff.dot(parent.getDirection()) / diff.length();
        float angleToStopAtMaxAngAcc =
                angularSpeed * angularSpeed / 2 / angularAccelerationMagnitude;
        targetAngle = angleDest;

        float angularSpeedMax = parent.getAngularSpeedMax();


        // Finished turning so angular acceleration is zero
        if(angleDest > 0.9999F && parent.getAngularSpeed() <= 0)
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

    public Vector3f rotatedDirection(float radian)
    {
        if(target == null)
            return new Vector3f(0, 0, 0);
        Vector3f direction = parent.getDirection();
        Vector3f destinationVector = new Vector3f(target);
        destinationVector.sub(parent.getPosition());
        return GameMath.rotatedDirection(radian, direction, destinationVector);
    }

    public float getTargetAngle() {
        return targetAngle;
    }

    public void setTarget(Vector3f target) {
        this.target = target;
    }

    public void draw(Graphics2D g2d) {

        float size = parent.getSize();
        if(target != null && target instanceof PositionInformation)
        {
            Vector3f p = ((PositionInformation)target).getPosition();
            g2d.drawLine((int)(p.x - size), (int)(p.y), (int)(p.x + size), (int)(p.y));
            g2d.drawLine((int)(p.x), (int)(p.y - size), (int)(p.x), (int)(p.y + size));
        }

        if(target != null)
        {
            g2d.drawLine((int)(target.x - size), (int)(target.y - size), (int)(target.x + size), (int)(target.y + size));
            g2d.drawLine((int)(target.x + size), (int)(target.y - size), (int)(target.x - size), (int)(target.y + size));
        }
    }

    @Override
    public void setParent(GameObject parent) {

    }

    @Override
    public NetworkComponent getNetworkComponent() {
        return networkComponent;
    }
}
