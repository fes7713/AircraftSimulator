package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.Aircraft.FlightController.FlightControllerInterface;
import aircraftsimulator.GameObject.Aircraft.FlightController.SimpleFlightController;
import aircraftsimulator.GameObject.Aircraft.Thruster.SimpleThruster;
import aircraftsimulator.GameObject.DestructibleObject;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.List;
import java.util.stream.Stream;

public class Aircraft extends DestructibleObject implements AircraftInterface{
    private Vector3f velocity;
    private Vector3f acceleration;
    private Vector3f direction;
    private float angularSpeed;
    private float angularAcceleration;
    private float angularAccelerationMagnitude;
    private float maxGAcceleration;
    private float angularSpeedMax;

    private final FlightControllerInterface flightControl;
    private SimpleThruster thruster;
    private AirResistance airResistance;

    public static final float THRUSTER_MAGNITUDE = 1F;
    public static final float FLIGHT_CONTROLLER_INTERVAL = 1F;
    public static final float ANGULAR_ACCELERATION = 0.01F;
    public static final float MAX_G_FORCE = 1F;
    public static final float AIR_RESISTANCE_COEFFICIENT = 0.02F;
    public static final float ANGULAR_SPEED_MAX = 0.05F;

    public Aircraft(Vector3f position, Color color, float size, float health) {
        this(new SimpleFlightController(FLIGHT_CONTROLLER_INTERVAL), position, color, size, health);
    }

    public Aircraft(FlightControllerInterface fci, Vector3f position, Color color, float size, float health){
        this(fci, position, color, size, health, THRUSTER_MAGNITUDE);
    }
    public Aircraft(FlightControllerInterface fci, Vector3f position, Color color, float size, float health, float thrusterMagnitude){
        this(fci, position, new Vector3f(-1 , -1, 0), color, size, health, thrusterMagnitude);
    }

    public Aircraft(FlightControllerInterface fci, Vector3f position, Vector3f velocity, Color color, float size, float health, float thrusterMagnitude) {
        super(position, color, size, health);
        flightControl = fci;
        fci.setParent(this);
        thruster = new SimpleThruster(this, thrusterMagnitude);
        airResistance = new AirResistance(this, AIR_RESISTANCE_COEFFICIENT);
        acceleration = new Vector3f();
        this.velocity = velocity;
        direction = new Vector3f(velocity);
        direction.normalize();
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
        angularAcceleration = 0;

        Vector3f waypoint = flightControl.nextPoint(delta);

        if(waypoint != null)
        {
            angularAcceleration = flightControl.calculateAngularAcceleration(delta);
            angularSpeed += angularAcceleration * delta;
            float angle = angularSpeed * delta;

            Vector3f directionNew = flightControl.rotatedDirection(angle);
            direction.set(directionNew);
        }

//        acceleration.set(0, 0, 0);
//        Stream.of(thruster, airResistance).forEach(forceApplier -> {
//            acceleration.add(forceApplier.generateForce());
//        });

        acceleration.set(flightControl.calculateLinearAcceleration(delta));

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
        direction2D.normalize();
        direction2D.scale(size);
        g2d.drawLine((int)position.x, (int)position.y, (int)(position.x + direction2D.x), (int)(position.y + direction2D.y));
        String text = String.format("Speed : %.5f\nAngular Speed : %.5f\nAngular Acceleration : %.5f\nG : %.5f\nTarget Angle : %.5f",
                velocity.length(),
                angularSpeed,
                angularAcceleration,
                velocity.length() * angularSpeed,
                flightControl.getTargetAngle());
        int y = (int)(position.y - 5);
        for (String line : text.split("\n"))
            g2d.drawString(line, (int)position.x + 5, y += g2d.getFontMetrics().getHeight());
        Vector3f waypoint = flightControl.nextPoint(0);

        if(waypoint != null)
        {
            g2d.drawLine((int)(waypoint.x - size), (int)(waypoint.y - size), (int)(waypoint.x + size), (int)(waypoint.y + size));
            g2d.drawLine((int)(waypoint.x + size), (int)(waypoint.y - size), (int)(waypoint.x - size), (int)(waypoint.y + size));
        }
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

    @Override
    public float getAngularSpeed() {
        return angularSpeed;
    }

    @Override
    public float getAngularAcceleration() {
        return angularAcceleration;
    }

    @Override
    public float getAngularAccelerationMagnitude() {
        return angularAccelerationMagnitude;
    }

    @Override
    public float getAngularSpeedMax()
    {
        return angularSpeedMax;
    }

    @Override
    public List<ForceApplier> getForceList() {
        return List.of(thruster, airResistance);
    }
}
