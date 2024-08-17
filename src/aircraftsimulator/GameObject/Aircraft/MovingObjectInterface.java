package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.GameObjectInterface;

import javax.vecmath.Vector3f;

public interface MovingObjectInterface extends GameObjectInterface {
    Vector3f getDirection();
    Vector3f getVelocity();
    float getRange();
    float getMinimumSpeed();
    void addForce(Vector3f force);
}
