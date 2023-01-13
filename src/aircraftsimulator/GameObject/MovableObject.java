package aircraftsimulator.GameObject;

import aircraftsimulator.GameObject.MovePolicy.MovePolicy;
import aircraftsimulator.GameObject.MovePolicy.SimpleMovePolicy;

import javax.vecmath.Vector2f;
import java.awt.*;

public class MovableObject extends GameObject implements Movable{
    Vector2f velocity;
    Vector2f acceleration;

    MovePolicy policy;

    int mass;

    public MovableObject(Vector2f position, Color color, float size) {
        super(position, color, size);
        policy = new SimpleMovePolicy(this);
    }

    @Override
    public Vector2f getVelocity() {
        return velocity;
    }

    @Override
    public Vector2f getAcceleration() {
        return acceleration;
    }

    @Override
    public void addForce(Vector2f force) {
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
