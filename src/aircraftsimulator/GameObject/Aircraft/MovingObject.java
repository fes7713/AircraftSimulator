package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.GameObject;
import aircraftsimulator.GameObject.Team;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.List;

public class MovingObject extends GameObject implements MovingObjectInterface{
    protected final Vector3f velocity;
    protected final Vector3f direction;

    protected final AirResistance airResistance;

    public static final float AIR_RESISTANCE_COEFFICIENT = 0.02F;

    public MovingObject(Team team, Vector3f position, Vector3f velocity, Color color, float size) {
        this(team, position, velocity, color, size, AIR_RESISTANCE_COEFFICIENT);
    }

    public MovingObject(Team team, Vector3f position, Vector3f velocity, Color color, float size, float airResistanceCoefficient) {
        super(team, position, color, size);
        airResistance = new AirResistance(this, airResistanceCoefficient);
        this.velocity = velocity;
        direction = new Vector3f(velocity);
        direction.normalize();
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
}
