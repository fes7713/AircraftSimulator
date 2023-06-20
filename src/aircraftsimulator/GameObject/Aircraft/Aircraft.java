package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.DestructibleObject;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.stream.Stream;

public class Aircraft extends DestructibleObject {
    private Vector3f velocity;
    private Vector3f acceleration;
    private Vector3f direction;
    private float angularSpeed;
    private float angularAcceleration;
    private float angularAccelerationMagnitude;
    private float maxGAcceleration;
    private float angularSpeedMax;


    // TODO
    private float targetAngle;

    private final FlightControllerInterface flightControl;
    private SimpleThruster thruster;
    private AirResistance airResistance;

    public static final float THRUSTER_MAGNITUDE = 1F;
    public static final float FLIGHT_CONTROLLER_INTERVAL = 1F;
    public static final float ANGULAR_ACCELERATION = 0.002F;
    public static final float MAX_G_FORCE = 1F;
    public static final float AIR_RESISTANCE_COEFFICIENT = 0.02F;
    public static final float ANGULAR_SPEED_MAX = 0.01F;

    public Aircraft(Vector3f position, Color color, float size, float health) {
        this(new SimpleFlightController(FLIGHT_CONTROLLER_INTERVAL), position, color, size, health);

    }

    public Aircraft(FlightControllerInterface fci, Vector3f position, Color color, float size, float health) {
        super(position, color, size, health);
        flightControl = fci;
        fci.setParent(this);
        thruster = new SimpleThruster(this, THRUSTER_MAGNITUDE);
        airResistance = new AirResistance(this, AIR_RESISTANCE_COEFFICIENT);
        acceleration = new Vector3f();
        // TODO
        velocity = new Vector3f(-1 , -1, 0);
        // TODO
        direction = new Vector3f(-1, -1, 0);
        angularSpeed = 0;
        // TODO
        angularAcceleration = ANGULAR_ACCELERATION;
        angularSpeedMax = ANGULAR_SPEED_MAX;
        angularAccelerationMagnitude = ANGULAR_ACCELERATION;
        maxGAcceleration = MAX_G_FORCE;
    }

    public void update(float delta)
    {
        float maxAngularVelocity = maxGAcceleration / velocity.length();

        Vector3f waypoint = flightControl.nextPoint(delta);
        Vector3f waypointVector = new Vector3f(waypoint);
        waypointVector.sub(position);
        float angleDest = waypointVector.dot(direction) / waypointVector.length();
        float angleToStopAtMaxAngAcc = angularSpeed * angularSpeed / 2 / angularAccelerationMagnitude;
        targetAngle = angleDest;
        if(angleDest > 0.99F && angularAcceleration < 0)
            angularAcceleration = 0;
        else if(Math.cos(angleToStopAtMaxAngAcc) < angleDest)
            angularAcceleration = -angularAccelerationMagnitude;
        else
            angularAcceleration = angularAccelerationMagnitude;
//        if(Math.cos(angleToStopAtMaxAngAcc) < angleDest && angularVelocity > 0)
//            angularAcceleration = - angularAccelerationMagnitude;
//        else if(Math.cos(angleToStopAtMaxAngAcc) < angleDest && angularVelocity <= 0)
//            angularAcceleration = 0;
//        else
//            angularAcceleration = angularAccelerationMagnitude;
        float angularSpeedOld = angularSpeed;
        angularSpeed += angularAcceleration * delta;

        // Clamp
        angularSpeed = Math.max(Math.min(angularSpeed, angularSpeedMax), 0);

        // Actual angular acceleration
        angularAcceleration = (angularSpeed - angularSpeedOld) / delta;
        float angle = angularSpeed * delta;
//        if(angle > 0)
//        {
//            Vector3f directionNew = flightControl.rotatedDirection(angle);
//            direction.set(directionNew);
//        }
        Vector3f directionNew = flightControl.rotatedDirection(angularSpeed);
        direction.set(directionNew);
        acceleration.set(0, 0, 0);
        Stream.of(thruster, airResistance).forEach(forceApplier -> {
            acceleration.add(forceApplier.generateForce());
        });
        Vector3f accelerationScaled = new Vector3f(acceleration);
        accelerationScaled.scale(delta);

        velocity.add(accelerationScaled);
        Vector3f velocityScaled = new Vector3f(velocity);
        velocityScaled.scale(delta);

        position.add(velocityScaled);
    }

    // TODO
    public void setTarget(DestructibleObject target)
    {
        flightControl.setTarget(target);
    }

    @Override
    public void draw(Graphics2D g2d) {
        super.draw(g2d);
        Vector2f direction2D = new Vector2f(direction.x, direction.y);
        direction2D.scale(size);
        g2d.drawLine((int)position.x, (int)position.y, (int)(position.x + (int)direction2D.x), (int)(position.y + (int)direction2D.y));
        String text = String.format("Speed : %.5f\nAngular Speed : %.5f\nAngular Acceleration : %.5f\nG : %.5f\nTarget Angle : %.5f",
                velocity.length(),
                angularSpeed,
                angularAcceleration,
                velocity.length() * angularSpeed,
                targetAngle);
        int y = (int)(position.y - 5);
        for (String line : text.split("\n"))
            g2d.drawString(line, (int)position.x + 5, y += g2d.getFontMetrics().getHeight());

    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public Vector3f getAcceleration() {
        return acceleration;
    }

    public Vector3f getDirection()
    {
        return direction;
    }
}
