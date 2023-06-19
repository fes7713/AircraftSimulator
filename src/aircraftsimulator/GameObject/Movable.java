package aircraftsimulator.GameObject;

import aircraftsimulator.GameObject.MovePolicy.MovePolicy;

import javax.vecmath.Vector3f;

public interface Movable {
    Vector3f getVelocity();
    Vector3f getAcceleration();
    void addForce(Vector3f force);
    void move(float delta);
    void setMovePolicy(MovePolicy policy);
}
