package aircraftsimulator.GameObject.Aircraft.Radar.DetectPredicate;

import aircraftsimulator.GameObject.GameObjectInterface;
import aircraftsimulator.GameObject.PositionnInterface;

import javax.vecmath.Vector3f;

public interface DetectPredicate{
    boolean test(PositionnInterface testingObject);
    void setTargetVector(Vector3f targetVector);
    void setParent(GameObjectInterface parent);
    DetectPredicate copy();
}
