package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.Aircraft.CentralStrategy.CentralStrategy;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.*;
import aircraftsimulator.GameObject.Aircraft.Communication.ReceiverInterface;
import aircraftsimulator.GameObject.Aircraft.Communication.SenderInterface;
import aircraftsimulator.GameObject.Aircraft.FlightController.AdvancedFlightController;
import aircraftsimulator.GameObject.Aircraft.FlightController.FlightControllerInterface;
import aircraftsimulator.GameObject.Aircraft.FlightController.SimpleFlightController;
import aircraftsimulator.GameObject.Aircraft.FlightController.SwitchValueFlightController;
import aircraftsimulator.GameObject.Aircraft.Spawner.WeaponSystem;
import aircraftsimulator.GameObject.Aircraft.Thruster.SimpleThruster;
import aircraftsimulator.GameObject.Aircraft.Thruster.Thruster;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.DestructibleMovingObject;
import aircraftsimulator.GameObject.Team;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.List;

public class Aircraft extends DestructibleMovingObject implements AircraftInterface, ReceiverInterface, SenderInterface, Cloneable, GuideNetwork {
    private float angularSpeed;
    private float angularAcceleration;
    private final float angularAccelerationMagnitude;
    private final float maxG;

    protected final CentralStrategy centralStrategy;
    protected final FlightControllerInterface flightControl;
    protected Thruster thruster;

    public static final float THRUSTER_MAGNITUDE = 1F;
    public static final float THRUSTER_FUEL = 3600F;
    public static final float FLIGHT_CONTROLLER_INTERVAL = 1F;
    public static final float ANGULAR_ACCELERATION = 0.01F;
    public static final float MAX_G_FORCE = 0.5F;

    public Aircraft(Aircraft a) {
        super(a);

        switch (a.flightControl)
        {
            case SwitchValueFlightController ignored -> flightControl = new SwitchValueFlightController<>(a.flightControl.getInterval());
            case AdvancedFlightController ignored1 -> flightControl = new AdvancedFlightController(a.flightControl.getInterval());
            case SimpleFlightController ignored2 -> flightControl = new SimpleFlightController(a.flightControl.getInterval());

            default -> throw new RuntimeException("Error in copy constructor of Aircraft");
        }
        centralStrategy = new CentralStrategy(this);
        flightControl.setParent(this);
        thruster = a.thruster.clone();
        angularSpeed = 0;
        angularAcceleration = a.angularAcceleration;
        angularAccelerationMagnitude = a.angularAccelerationMagnitude;
        maxG = a.maxG;
        addComponent(thruster);
        for(Component c: a.components)
            if(!(c instanceof Thruster))
                addComponent(c.clone());
    }

    public Aircraft(Team team, Vector3f position, Color color, float size, float health) {
        this(team, new SimpleFlightController(FLIGHT_CONTROLLER_INTERVAL), position, color, size, health);
    }

    public Aircraft(Team team, FlightControllerInterface fci, Vector3f position, Color color, float size, float health){
        this(team, fci, position, color, size, health, THRUSTER_MAGNITUDE, THRUSTER_FUEL);
    }

    public Aircraft(Team team, FlightControllerInterface fci, Vector3f position, Color color, float size, float health, float thrusterMagnitude, float fuel){
        this(team, fci, position, new Vector3f(-1 , -1, 0), color, size, health, thrusterMagnitude, fuel);
    }
    public Aircraft(Team team, FlightControllerInterface fci, Vector3f position, Vector3f velocity, Color color, float size, float health, float thrusterMagnitude) {
        this(team, fci, position, velocity, color, size, health, thrusterMagnitude, THRUSTER_FUEL);
    }

    public Aircraft(Team team, FlightControllerInterface fci, Vector3f position, Vector3f velocity, Color color, float size, float health, float thrusterMagnitude, float fuel) {
        super(team, position, velocity, color, size, health);
        centralStrategy = new CentralStrategy(this);
        flightControl = fci;
        fci.setParent(this);
        thruster = new SimpleThruster(this, thrusterMagnitude, fuel);
        angularSpeed = 0;
        angularAcceleration = ANGULAR_ACCELERATION;
        angularAccelerationMagnitude = ANGULAR_ACCELERATION;
        maxG = MAX_G_FORCE;
        addComponent(thruster);
    }

    public void update(float delta)
    {
        centralStrategy.update(delta);
        angularAcceleration = 0;

        flightControl.update(delta);

        angularAcceleration = flightControl.calculateAngularAcceleration(delta);
        angularSpeed += angularAcceleration * delta;

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
        super.draw(g2d);
        flightControl.draw(g2d);
        Vector2f direction2D = new Vector2f(direction.x, direction.y);
        direction2D.normalize();
        direction2D.scale(size);
        g2d.drawLine((int)position.x, (int)position.y, (int)(position.x + direction2D.x), (int)(position.y + direction2D.y));
        String text = String.format("Height : %.5f\nThruster : %.5f\nFuel : %.5f\nSpeed : %.5f\nAngular Speed : %.5f\nAngular Acceleration : %.5f\nG : %.5f\nTarget Angle : %.5f",
                position.z,
                thruster.getMagnitude(),
                thruster.getFuel(),
                velocity.length(),
                angularSpeed,
                angularAcceleration,
                velocity.length() * angularSpeed,
                flightControl.getTargetAngle());
        int y = (int)(position.y - 5);
//        for (String line : text.split("\n"))
//            g2d.drawString(line, (int)position.x + 5, y += g2d.getFontMetrics().getHeight());
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
        removeComponent(this.thruster);
        addComponent(thruster);
        this.thruster = thruster;
        flightControl.configurationChanged();
    }

    @Override
    public float getRange()
    {
        float L = (float)Math.sqrt(thruster.getMagnitude() * airResistance.getCoefficient());
        float v_inf = (float)Math.sqrt(thruster.getMagnitude() / airResistance.getCoefficient());

        float speed = velocity.length();
        float log_coef = v_inf / L;
        float sinh_coef = speed / v_inf;

        float max_time = thruster.getMaxTime();
        float Ltime = L * max_time;

        double tanh = Math.tanh(Ltime);
        float max_speed = (float)(
                v_inf *
                        (speed + v_inf * tanh)
                        /
                        (v_inf + speed * tanh));

        double no_thruster_distance = (Math.log(max_speed / minimumSpeed) / airResistance.getCoefficient());

        return (float) (log_coef * Math.log(Math.cosh(Ltime) + sinh_coef * Math.sinh(Ltime)) + no_thruster_distance);
    }

    public void connectToGuidance(Guided guidedObject, PositionInformation keyInformation)
    {
        centralStrategy.addToGuidance(guidedObject, keyInformation);
    }

    @Override
    public void disconnectFromGuidance(Guided guidedObject) {
        centralStrategy.removeFromGuidance(guidedObject);
    }

    @Override
    public void addComponent(Component component)
    {
        component.setParent(this);
        components.add(component);
        if(component instanceof WeaponSystem weaponSystem)
            centralStrategy.addWeaponSystem(weaponSystem);
    }

    @Override
    public void removeComponent(Component component)
    {
        components.remove(component);
//        router.removeRouting(component);
        if(component instanceof WeaponSystem w)
            centralStrategy.removeWeaponSystem(w);
    }

    @Override
    public void receive(@Nullable Information information) {
        flightControl.setTarget(information);
        centralStrategy.receive(information);
    }

    @Override
    public <T extends PositionInformation> PositionInformation send(Class<T> type) {
        if(type == MotionInformation.class)
        {
            return new MotionInformationImp(this, position, velocity, getAcceleration(), direction);
        }
        else if(type == PositionInformation.class)
        {
            return new PositionInformationImp(this, position);
        }
        System.err.println("Type error in Aircraft.java send(Class<T>)");
        return super.send(type);
    }

    @Override
    public Aircraft clone() {
        return new Aircraft(this);
    }
}
