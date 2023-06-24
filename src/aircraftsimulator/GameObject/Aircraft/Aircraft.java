package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.MotionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.ReceiverInterface;
import aircraftsimulator.GameObject.Aircraft.Communication.SenderInterface;
import aircraftsimulator.GameObject.Aircraft.FlightController.FlightControllerInterface;
import aircraftsimulator.GameObject.Aircraft.FlightController.SimpleFlightController;
import aircraftsimulator.GameObject.Aircraft.Thruster.SimpleThruster;
import aircraftsimulator.GameObject.Aircraft.Thruster.Thruster;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.DestructibleObject;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Aircraft extends DestructibleObject implements AircraftInterface, ReceiverInterface, SenderInterface {
    private final Vector3f velocity;
    private final Vector3f direction;

    private float angularSpeed;
    private float angularAcceleration;
    private final float angularAccelerationMagnitude;
    private final float maxG;

    private final FlightControllerInterface flightControl;
    private Thruster thruster;
    private final AirResistance airResistance;
    private final List<Component> components;

    public static final float THRUSTER_MAGNITUDE = 1F;
    public static final float FLIGHT_CONTROLLER_INTERVAL = 1F;
    public static final float ANGULAR_ACCELERATION = 0.01F;
    public static final float MAX_G_FORCE = 0.5F;
    public static final float AIR_RESISTANCE_COEFFICIENT = 0.02F;

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
        this.velocity = velocity;
        direction = new Vector3f(velocity);
        direction.normalize();
        angularSpeed = 0;
        angularAcceleration = ANGULAR_ACCELERATION;
        angularAccelerationMagnitude = ANGULAR_ACCELERATION;
        maxG = MAX_G_FORCE;
        components = new ArrayList<>();
    }

    public void update(float delta)
    {
        components.forEach(o -> o.update(delta));
        angularAcceleration = 0;

        Vector3f waypoint = flightControl.nextPoint(delta);

        angularAcceleration = flightControl.calculateAngularAcceleration(delta);
        angularSpeed += angularAcceleration * delta;
        float angle = angularSpeed * delta;

        if(angle != 0) {
            Vector3f directionNew = flightControl.rotatedDirection(angle);
            direction.set(directionNew);
        }

        Vector3f accelerationScaled = new Vector3f(flightControl.calculateLinearAcceleration(delta));
        accelerationScaled.scale(delta);

        velocity.add(accelerationScaled);
        Vector3f velocityScaled = new Vector3f(velocity);
        velocityScaled.scale(delta);

        position.add(velocityScaled);
    }

    @Override
    public void draw(Graphics2D g2d) {
        components.forEach(o -> o.draw(g2d));
        super.draw(g2d);
        Vector2f direction2D = new Vector2f(direction.x, direction.y);
        direction2D.normalize();
        direction2D.scale(size);
        g2d.drawLine((int)position.x, (int)position.y, (int)(position.x + direction2D.x), (int)(position.y + direction2D.y));
        String text = String.format("Height : %.5f\nThruster : %.5f\nSpeed : %.5f\nAngular Speed : %.5f\nAngular Acceleration : %.5f\nG : %.5f\nTarget Angle : %.5f",
                direction.z,
                thruster.generateForce().length(),
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
        Vector3f acceleration = new Vector3f(0, 0, 0);
        List<ForceApplier> forces = getForceList();
        for(ForceApplier force: forces)
            acceleration.add(force.generateForce());
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
    public float getAngularSpeedMax() {
        return getMaxG() / getVelocity().length();
    }

    public float getMaxG() {
        return maxG;
    }

    @Override
    public List<ForceApplier> getForceList() {
        return List.of(thruster, airResistance);
    }

    @Override
    public void setThruster(Thruster thruster) {
        this.thruster = thruster;
        flightControl.configurationChanged();
    }

    public void addComponent(Component component)
    {
        component.setParent(this);
        components.add(component);
    }

    @Override
    public void receive(Information information) {
        flightControl.setTarget(information);
    }

    @Override
    public <T extends Information> Information send(Class<T> type) {
        if(type == PositionInformation.class)
        {
            return new PositionInformation(position);
        }
        else if(type == MotionInformation.class)
        {
            return new MotionInformation(position, velocity, getAcceleration(), direction);
        }
        System.err.println("Type error in Aircraft.java send(Class<T>)");
        return super.send(type);
    }
}
