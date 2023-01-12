package aircraftsimulator.GameObject;

import javax.vecmath.Vector2f;

public interface Movable {
    Vector2f getVelocity();
    Vector2f getAcceleration();
    void move();
}
