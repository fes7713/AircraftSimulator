package aircraftsimulator.GameObject;

import aircraftsimulator.GameObject.MovePolicy.MovePolicy;
import aircraftsimulator.GameObject.MovePolicy.SimpleMovePolicy;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.awt.*;

public class MovableObject extends GameObject implements Movable{
    Vector3f velocity;
    Vector3f acceleration;

    MovePolicy policy;

    int mass;
    Vector3f coordinate;

    public MovableObject(Vector3f position, Color color, float size) {
        super(position, color, size);
        policy = new SimpleMovePolicy(this);
        coordinate = new Vector3f(0, 0, 0);
    }

    @Override
    public Vector3f getVelocity() {
        return velocity;
    }

    @Override
    public Vector3f getAcceleration() {
        return acceleration;
    }

    @Override
    public void addForce(Vector3f force) {
        acceleration.x += force.x / mass;
        acceleration.y += force.y / mass;
    }

    @Override
    public void move(float delta) {
        Vector2f destination = policy.destination();
        if(destination == null)
            return;

    }

    @Override
    public void setMovePolicy(MovePolicy policy) {
        this.policy = policy;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        velocity.x += acceleration.x * delta;
        velocity.y += acceleration.y * delta;
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;
    }
}
