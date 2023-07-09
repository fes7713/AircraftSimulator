package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.Aircraft.Spawner.Spawnable;
import aircraftsimulator.GameObject.GameObject;
import aircraftsimulator.GameObject.Team;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.List;

public class MovingObject extends GameObject implements MovingObjectInterface, Spawnable {
    protected final Vector3f velocity;
    protected final Vector3f direction;
    protected float minimumSpeed;

    protected final AirResistance airResistance;

    public static final float AIR_RESISTANCE_COEFFICIENT = 0.02F;
    public final static float MINIMUM_SPEED = 1;

    protected MovingObject(MovingObject m)
    {
        this(m.team, new Vector3f(), new Vector3f(), m.color, m.size, m.airResistance.getCoefficient());
        minimumSpeed = m.minimumSpeed;
    }

    public MovingObject(Team team, Vector3f position, Vector3f velocity, Color color, float size) {
        this(team, position, velocity, color, size, AIR_RESISTANCE_COEFFICIENT);
    }

    public MovingObject(Team team, Vector3f position, Vector3f velocity, Color color, float size, float airResistanceCoefficient) {
        super(team, position, color, size);
        airResistance = new AirResistance(this, airResistanceCoefficient);
        this.velocity = velocity;
        direction = new Vector3f(velocity);
        direction.normalize();
        minimumSpeed = MINIMUM_SPEED;
    }

    public void update(float delta)
    {
        super.update(delta);
        Vector3f accelerationScaled = new Vector3f(getAcceleration());
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
    public List<ForceApplier> getForceList() {
        return List.of(airResistance);
    }

    public float getRange() {
        if(minimumSpeed <= 0)
            return Float.MAX_VALUE;
        return (float)(Math.log(velocity.length() / minimumSpeed) / airResistance.getCoefficient());
    }

    @Override
    public void activate(Vector3f position, Vector3f velocity, Vector3f direction) {
        this.position.set(position);
        this.velocity.set(velocity);
        this.direction.set(direction);
    }
}
