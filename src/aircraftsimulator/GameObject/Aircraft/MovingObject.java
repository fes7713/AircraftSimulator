package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.GameObject;
import aircraftsimulator.GameObject.Team;

import javax.vecmath.Vector3f;
import java.awt.*;

public class MovingObject extends GameObject implements MovingObjectInterface {
    protected final Vector3f velocity;
    protected float minimumSpeed;
    protected Vector3f force;
    protected Vector3f acceleration;

    protected final AirResistance airResistance;

    public static final float AIR_RESISTANCE_COEFFICIENT = 400F;
    public final static float MINIMUM_SPEED = 1;

    public MovingObject(MovingObject m)
    {
        this(m.team, new Vector3f(), new Vector3f(), m.color, m.size, m.mass, m.airResistance.getCoefficient());
        minimumSpeed = m.minimumSpeed;
    }

    public MovingObject(Team team, Vector3f position, Vector3f velocity, Color color, float size, float mass) {
        this(team, position, velocity, color, size, mass, AIR_RESISTANCE_COEFFICIENT);
    }

    public MovingObject(Team team, Vector3f position, Vector3f velocity, Color color, float size, float mass, float airResistanceCoefficient) {
        super(team, position, color, size, mass);
        airResistance = new AirResistance(this, airResistanceCoefficient);
        acceleration = new Vector3f();
        force = new Vector3f();
        this.velocity = velocity;
        direction.set(new Vector3f(velocity));
        direction.normalize();
        minimumSpeed = MINIMUM_SPEED;
    }

    public void update(float delta)
    {
        super.update(delta);
        force.add(airResistance.generateForce());
        acceleration.set(force);
        acceleration.scale(1F/getMass());
        Vector3f accelerationScaled = new Vector3f(acceleration);
        accelerationScaled.scale(delta);

        velocity.add(accelerationScaled);
        Vector3f velocityScaled = new Vector3f(velocity);
        velocityScaled.scale(delta);

        position.add(velocityScaled);
        force.set(0, 0, 0);
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public void addForce(Vector3f force)
    {
        this.force.add(force);
    }

    public float getRange() {
        if(minimumSpeed <= 0)
            return Float.MAX_VALUE;
        return (float)(Math.log(velocity.length() / minimumSpeed) / airResistance.getCoefficient());
    }

    @Override
    public float getMinimumSpeed() {
        return minimumSpeed;
    }

}
