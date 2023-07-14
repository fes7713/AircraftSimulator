package aircraftsimulator.GameObject.Aircraft.Radar;

import aircraftsimulator.GameMath;
import aircraftsimulator.GameObject.Aircraft.Communication.ReceiverInterface;
import aircraftsimulator.GameObject.Aircraft.MovingObjectInterface;
import aircraftsimulator.GameObject.Aircraft.Radar.DetectPredicate.AngleDetect;
import aircraftsimulator.GameObject.Aircraft.Radar.DetectPredicate.MultiDetect;
import aircraftsimulator.GameObject.Aircraft.Radar.DetectPredicate.RangeDetect;
import aircraftsimulator.GameObject.GameObject;
import aircraftsimulator.GameObject.GameObjectInterface;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.awt.*;

public class AngleRadar extends SimpleRadar{
    private Vector3f direction;
    private final float angle;

    public AngleRadar(GameObjectInterface parent, float range, float angle, Vector3f direction, ReceiverInterface receiverInterface) {
        super(parent, range, receiverInterface, new MultiDetect(parent, new RangeDetect(parent, range), new AngleDetect(parent, direction, angle)));
        this.direction = direction;
        this.angle = angle;
    }

    public AngleRadar(MovingObjectInterface parent, float range, float angle, ReceiverInterface receiverInterface) {
        super(parent, range, receiverInterface, new MultiDetect(parent, new RangeDetect(parent, range), new AngleDetect(parent, angle)));
        this.direction = parent.getDirection();
        this.angle = angle;
    }

    // TODO
    // If parent is not receiverInterface then run time error.
    public AngleRadar(GameObjectInterface parent, float range, float angle, Vector3f direction) {
        this(parent, range, angle, direction, (ReceiverInterface)parent);
    }

    public AngleRadar(MovingObjectInterface parent, float range, float angle) {
        this(parent, range, angle, (ReceiverInterface)parent);
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(radarColor);
        double angleCos =
                Math.sqrt((direction.x * direction.x + direction.y * direction.y)
                        / (direction.x * direction.x + direction.y * direction.y + direction.z * direction.z));
        double length = range * angleCos;


        Vector3f center = parent.getPosition();
        double centerAngle = GameMath.directionToAngle(new Vector2f(direction.x, direction.y)) % 360;

        g2d.fillArc((int)(center.x - length), (int)(center.y - length), (int)(length * 2), (int)(length * 2), (int)(centerAngle - angle / 2), (int)angle);
    }

    @Override
    public void setParent(GameObject parent) {
        super.setParent(parent);
        if(parent instanceof MovingObjectInterface m)
            direction = m.getDirection();
    }
}
