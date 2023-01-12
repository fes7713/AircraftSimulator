package aircraftsimulator.GameObject;

import aircraftsimulator.GameObject.MovePolicy.MovePolicy;

import javax.vecmath.Vector2f;
import java.awt.*;

public class MovableObject extends GameObject implements Movable{
    Vector2f velocity;
    Vector2f acceleration;

    MovePolicy policy;

    public MovableObject(Vector2f position, Color color, float size) {
        super(position, color, size);
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
    public void move(float delta) {

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
