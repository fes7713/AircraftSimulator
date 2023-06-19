package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.DestructibleObject;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Aircraft extends DestructibleObject {
    private Vector3f velocity;
    private Vector3f acceleration;
    private Vector3f direction;
    private Vector3f maxRotation;

    // TODO
    private Map<String, Float> accelerationMap;

    private final FlightControllerInterface flightControl;
    private SimpleThruster thruster;

    public static final float THRUSTER_MAGNITUDE = 1F;
    public static final float FLIGHT_CONTROLLER_INTERVAL = 0;

    public Aircraft(Vector3f position, Color color, float size, float health) {
        this(new SimpleFlightController(FLIGHT_CONTROLLER_INTERVAL), position, color, size, health);

    }

    public Aircraft(FlightControllerInterface fci, Vector3f position, Color color, float size, float health) {
        super(position, color, size, health);
        flightControl = fci;
        fci.setParent(this);
        accelerationMap = new HashMap<>();
        thruster = new SimpleThruster(THRUSTER_MAGNITUDE);
        acceleration = new Vector3f();
        velocity = new Vector3f();
    }

    public void update(float delta)
    {
        // TODO
        acceleration.set(thruster.applyForce(new Vector3f(1, 0, 0)));
        Vector3f accelerationScaled = new Vector3f(acceleration);
        accelerationScaled.scale(delta);

        velocity.add(accelerationScaled);
        Vector3f velocityScaled = new Vector3f(velocity);
        velocityScaled.scale(delta);

        position.add(velocityScaled);
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
