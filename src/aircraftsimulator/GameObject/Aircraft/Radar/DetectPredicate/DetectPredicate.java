package aircraftsimulator.GameObject.Aircraft.Radar.DetectPredicate;

import aircraftsimulator.GameObject.GameObjectInterface;

import javax.vecmath.Vector3f;

public interface DetectPredicate {
    boolean test(GameObjectInterface testingObject);
    void setTargetVector(Vector3f targetVector);
    void setParent(GameObjectInterface parent);
}
