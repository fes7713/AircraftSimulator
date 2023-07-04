package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.MotionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.InformationNetwork;
import aircraftsimulator.GameObject.Aircraft.Communication.ReceiverInterface;
import aircraftsimulator.GameObject.Aircraft.Communication.SenderInterface;
import aircraftsimulator.GameObject.Aircraft.FlightController.FlightControllerInterface;
import aircraftsimulator.GameObject.Aircraft.FlightController.SimpleFlightController;
import aircraftsimulator.GameObject.Aircraft.Thruster.SimpleThruster;
import aircraftsimulator.GameObject.Aircraft.Thruster.Thruster;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.DestructibleMovingObject;
import aircraftsimulator.GameObject.Team;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Aircraft extends DestructibleMovingObject implements AircraftInterface, ReceiverInterface, SenderInterface {
    private float angularSpeed;
    private float angularAcceleration;
    private final float angularAccelerationMagnitude;
    private final float maxG;

    protected final FlightControllerInterface flightControl;
    protected Thruster thruster;
    protected final List<Component> components;
    protected final InformationNetwork network;

    public static final float THRUSTER_MAGNITUDE = 1F;
    public static final float FLIGHT_CONTROLLER_INTERVAL = 0.001F;
    public static final float ANGULAR_ACCELERATION = 0.01F;
    public static final float MAX_G_FORCE = 0.5F;


    public Aircraft(Team team, Vector3f position, Color color, float size, float health) {
        this(team, new SimpleFlightController(FLIGHT_CONTROLLER_INTERVAL), position, color, size, health);
    }

    public Aircraft(Team team, FlightControllerInterface fci, Vector3f position, Color color, float size, float health){
        this(team, fci, position, color, size, health, THRUSTER_MAGNITUDE);
    }

    public Aircraft(Team team, FlightControllerInterface fci, Vector3f position, Color color, float size, float health, float thrusterMagnitude){
        this(team, fci, position, new Vector3f(-1 , -1, 0), color, size, health, thrusterMagnitude);
    }

    public Aircraft(Team team, FlightControllerInterface fci, Vector3f position, Vector3f velocity, Color color, float size, float health, float thrusterMagnitude) {
        super(team, position, velocity, color, size, health);
        flightControl = fci;
        fci.setParent(this);
        thruster = new SimpleThruster(this, thrusterMagnitude);
        angularSpeed = 0;
        angularAcceleration = ANGULAR_ACCELERATION;
        angularAccelerationMagnitude = ANGULAR_ACCELERATION;
        maxG = MAX_G_FORCE;
        components = new ArrayList<>();
        network = new InformationNetwork();
    }

    public void update(float delta)
    {
        components.forEach(o -> o.update(delta));
        angularAcceleration = 0;

        flightControl.update(delta);

        angularAcceleration = flightControl.calculateAngularAcceleration(delta);
        angularSpeed += angularAcceleration * delta;
        // TODO need to remove if here
        if(angularSpeed < 0.00000001F)
            angularSpeed = 0;
        float angle = angularSpeed * delta;

        if(angle != 0) {
            Vector3f directionNew = flightControl.rotatedDirection(angle);
            direction.set(directionNew);
        }

        flightControl.calculateLinearAcceleration(delta);

        super.update(delta);
    }

    @Override
    public void draw(Graphics2D g2d) {
        components.forEach(o -> o.draw(g2d));
        super.draw(g2d);
        flightControl.draw(g2d);
        Vector2f direction2D = new Vector2f(direction.x, direction.y);
        direction2D.normalize();
        direction2D.scale(size);
        g2d.drawLine((int)position.x, (int)position.y, (int)(position.x + direction2D.x), (int)(position.y + direction2D.y));
        String text = String.format("Height : %.5f\nThruster : %.5f\nSpeed : %.5f\nAngular Speed : %.5f\nAngular Acceleration : %.5f\nG : %.5f\nTarget Angle : %.5f",
                position.z,
                thruster.generateForce().length(),
                velocity.length(),
                angularSpeed,
                angularAcceleration,
                velocity.length() * angularSpeed,
                flightControl.getTargetAngle());
        int y = (int)(position.y - 5);
        for (String line : text.split("\n"))
            g2d.drawString(line, (int)position.x + 5, y += g2d.getFontMetrics().getHeight());
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

    @Override
    public void addComponent(Component component)
    {
        component.setParent(this);
        components.add(component);
        if(component instanceof ReceiverInterface)
            network.addReceiver((ReceiverInterface)component);
    }

    @Override
    public void addToNetwork(ReceiverInterface receiverInterface){
        network.addReceiver(receiverInterface);
    }

    @Override
    public void receive(@Nullable Information information) {
        flightControl.setTarget(information);
        network.receive(information);
    }

    @Override
    public <T extends Information> Information send(Class<T> type) {
        if(type == PositionInformation.class)
        {
            return new PositionInformation(this, position);
        }
        else if(type == MotionInformation.class)
        {
            return new MotionInformation(this, position, velocity, getAcceleration(), direction);
        }
        System.err.println("Type error in Aircraft.java send(Class<T>)");
        return super.send(type);
    }

    @Override
    public void remove() {
        super.remove();
        if(parent != null && parent instanceof Aircraft)
            ((Aircraft)parent).network.removeReceiver(this);
    }
}
