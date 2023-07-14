package aircraftsimulator.GameObject.Aircraft.Radar.DetectPredicate;

import aircraftsimulator.GameObject.GameObjectInterface;

import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiDetect extends BaseDetect{
    private final List<DetectPredicate> detectPredicates;

    public MultiDetect(GameObjectInterface parent) {
        super(parent);
        this.detectPredicates = new ArrayList<>();
    }

    public MultiDetect(GameObjectInterface parent, DetectPredicate... detectPredicates) {
        super(parent);
        this.detectPredicates = new ArrayList<>(Arrays.asList(detectPredicates));
    }

    @Override
    public boolean test(GameObjectInterface testingObject) {
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
        for(DetectPredicate d: detectPredicates)
            d.setParent(parent);
    }

    public void addPredicate(DetectPredicate detectPredicate)
    {
        detectPredicates.add(detectPredicate);
    }

    public List<DetectPredicate> getDetectPredicates()
    {
        return detectPredicates;
    }
}
