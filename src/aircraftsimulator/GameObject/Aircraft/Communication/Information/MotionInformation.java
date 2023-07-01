package aircraftsimulator.GameObject.Aircraft.Communication.Information;

import aircraftsimulator.GameObject.GameObject;

import javax.vecmath.Vector3f;

public class MotionInformation extends PositionInformation{
    private final Vector3f velocity;
    private final Vector3f acceleration;
    private final Vector3f direction;

    public MotionInformation(GameObject source, Vector3f position, Vector3f velocity, Vector3f acceleration, Vector3f direction) {
        super(source, position);
        this.velocity = new Vector3f(velocity);
        this.acceleration = new Vector3f(acceleration);
        this.direction = new Vector3f(direction);
    }

    public Vector3f getVelocity() {
        return new Vector3f(velocity);
    }

    public Vector3f getAcceleration() {
        return new Vector3f(acceleration);
    }

    public Vector3f getDirection() {
        return new Vector3f(direction);
    }
}
