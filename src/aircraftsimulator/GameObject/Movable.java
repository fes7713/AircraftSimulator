package aircraftsimulator.GameObject;

import aircraftsimulator.GameObject.MovePolicy.MovePolicy;

import javax.vecmath.Vector2f;

public interface Movable {
    Vector2f getVelocity();
    Vector2f getAcceleration();
    void addForce(Vector2f force);
    void move(float delta);
    void setMovePolicy(MovePolicy policy);
}
