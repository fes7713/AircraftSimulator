package aircraftsimulator.GameObject.Aircraft.Communication.Information;

import aircraftsimulator.GameObject.GameObjectInterface;

import javax.vecmath.Vector3f;

public class MotionInformationImp extends PositionInformationImp implements MotionInformation {
    private final Vector3f velocity;
    private final Vector3f acceleration;
    private final Vector3f direction;

    public MotionInformationImp(GameObjectInterface source, Vector3f position, Vector3f velocity, Vector3f acceleration, Vector3f direction) {
        super(source, position);
        this.velocity = new Vector3f(velocity);
        this.acceleration = new Vector3f(acceleration);
        this.direction = new Vector3f(direction);
    }

    public MotionInformationImp(MotionInformation m) {
        super(m.getSource(), m.getPosition());
        this.velocity = new Vector3f(m.getVelocity());
        this.acceleration = new Vector3f(m.getAcceleration());
        this.direction = new Vector3f(m.getDirection());
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
