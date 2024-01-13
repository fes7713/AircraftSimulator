package aircraftsimulator.GameObject.Aircraft.Radar.DetectPredicate;

import aircraftsimulator.GameObject.GameObjectInterface;
import aircraftsimulator.GameObject.PositionnInterface;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiDetect extends BaseDetect{
    private List<DetectPredicate> detectPredicates;

    public MultiDetect(GameObjectInterface parent) {
        super(parent);
        this.detectPredicates = new ArrayList<>();
    }

    public MultiDetect(GameObjectInterface parent, DetectPredicate... detectPredicates) {
        super(parent);
        this.detectPredicates = new ArrayList<>(Arrays.asList(detectPredicates));
    }

    @Override
    public boolean test(PositionnInterface testingObject) {
        Vector3f v = new Vector3f(testingObject.getPosition());
        v.sub(parent.getPosition());

        for(DetectPredicate d: detectPredicates)
        {
            d.setTargetVector(v);
            if(!d.test(testingObject))
                return false;
        }

        return true;
    }

    @Override
    public void setParent(GameObjectInterface parent) {
        super.setParent(parent);
        for(DetectPredicate d: detectPredicates)
            d.setParent(parent);
    }

    @Override
    public MultiDetect copy() {
        return clone();
    }

    public void addPredicate(DetectPredicate detectPredicate)
    {
        detectPredicates.add(detectPredicate);
    }

    public List<DetectPredicate> getDetectPredicates()
    {
        return detectPredicates;
    }

    @Override
    public MultiDetect clone() {
        MultiDetect clone = (MultiDetect) super.clone();
        clone.detectPredicates = new ArrayList<>();
        for(DetectPredicate d: detectPredicates)
            clone.detectPredicates.add(d.copy());

        return clone;
    }
}
