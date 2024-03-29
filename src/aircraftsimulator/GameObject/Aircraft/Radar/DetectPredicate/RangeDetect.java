package aircraftsimulator.GameObject.Aircraft.Radar.DetectPredicate;

import aircraftsimulator.GameObject.GameObjectInterface;
import aircraftsimulator.GameObject.PositionnInterface;

import javax.vecmath.Vector3f;

public class RangeDetect extends BaseDetect{
    protected final float range;

    public RangeDetect(GameObjectInterface parent, float range)
    {
        super(parent);
        this.range = range;
    }

    @Override
    public boolean test(PositionnInterface testingObject) {
        float rangeSquared = range * range;
        if(targetVector != null)
        {
            return targetVector.lengthSquared() < rangeSquared;
        }
        Vector3f v = new Vector3f(parent.getPosition());
        v.sub(testingObject.getPosition());
        return v.lengthSquared() < rangeSquared;
    }

    @Override
    public DetectPredicate copy() {
        return clone();
    }
}
