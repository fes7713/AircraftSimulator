package aircraftsimulator.GameObject.Aircraft.Radar.DetectPredicate;

import aircraftsimulator.GameObject.GameObjectInterface;

import javax.vecmath.Vector3f;

public abstract class BaseDetect implements DetectPredicate{
    protected GameObjectInterface parent;
    protected Vector3f targetVector;

    public BaseDetect(GameObjectInterface parent)
    {
        this.parent = parent;
        targetVector = null;
    }

    @Override
    public boolean test(GameObjectInterface testingObject) {
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
}
