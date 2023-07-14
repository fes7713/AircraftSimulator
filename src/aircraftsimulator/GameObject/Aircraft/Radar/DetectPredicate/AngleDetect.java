package aircraftsimulator.GameObject.Aircraft.Radar.DetectPredicate;

import aircraftsimulator.GameObject.Aircraft.MovingObjectInterface;
import aircraftsimulator.GameObject.GameObjectInterface;

import javax.vecmath.Vector3f;

public class AngleDetect extends BaseDetect{
    private final float angle;
    private Vector3f direction;

    public AngleDetect(GameObjectInterface parent, Vector3f direction, float angle) {
        super(parent);
        this.direction = direction;
        this.angle = angle;
    }

    public AngleDetect(MovingObjectInterface parent, float angle) {
        super(parent);
        this.direction = parent.getDirection();
        this.angle = angle;
    }

    @Override
    public boolean test(GameObjectInterface testingObject) {
        super.test(testingObject);
        float angleCos = direction.dot(targetVector) / direction.length() / targetVector.length();
        return angleCos > Math.cos(Math.toRadians(angle / 2));
    }

    @Override
    public void setParent(GameObjectInterface parent) {
        super.setParent(parent);
        if(parent instanceof MovingObjectInterface m)
            direction = m.getDirection();
    }
}
