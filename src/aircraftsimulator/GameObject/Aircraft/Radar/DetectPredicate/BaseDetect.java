package aircraftsimulator.GameObject.Aircraft.Radar.DetectPredicate;

import aircraftsimulator.GameObject.GameObjectInterface;
import aircraftsimulator.GameObject.PositionnInterface;

import javax.vecmath.Vector3f;

public abstract class BaseDetect implements DetectPredicate, Cloneable{
    protected GameObjectInterface parent;
    protected Vector3f targetVector;

    public BaseDetect(GameObjectInterface parent)
    {
        this.parent = parent;
        targetVector = null;
    }

    @Override
    public boolean test(PositionnInterface testingObject) {
        if(targetVector == null)
            throw new RuntimeException("Set targetVector first");
        return false;
    }

    @Override
    public void setTargetVector(Vector3f targetVector) {
        this.targetVector = targetVector;
    }

    @Override
    public void setParent(GameObjectInterface parent) {
        this.parent = parent;
    }

    @Override
    public BaseDetect clone() {
        BaseDetect clone = null;
        try {
            clone = (BaseDetect) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
        clone.targetVector = null;
        return clone;
    }
}
