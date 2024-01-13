package aircraftsimulator.GameObject.Aircraft.Radar.Radar;

import aircraftsimulator.Environment;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.LaserInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformationImp;
import aircraftsimulator.GameObject.Aircraft.Communication.ReceiverInterface;
import aircraftsimulator.GameObject.Aircraft.MovingObjectInterface;
import aircraftsimulator.GameObject.Aircraft.Radar.DetectPredicate.AngleDetect;
import aircraftsimulator.GameObject.Aircraft.Radar.DetectPredicate.MultiDetect;
import aircraftsimulator.GameObject.Aircraft.Radar.DetectPredicate.RangeDetect;
import aircraftsimulator.GameObject.GameObject;
import aircraftsimulator.GameObject.GameObjectInterface;
import aircraftsimulator.PaintDrawer;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.List;

public class AngleRadar extends SimpleRadar{
    private Vector3f direction;
    private final float angle;

    public AngleRadar(GameObjectInterface parent, float frequency, float range, float angle, Vector3f direction, ReceiverInterface receiverInterface) {
        super(parent, frequency, range, receiverInterface, new MultiDetect(parent, new RangeDetect(parent, range), new AngleDetect(parent, direction, angle)));
        this.direction = direction;
        this.angle = angle;
    }

    public AngleRadar(MovingObjectInterface parent, float frequency, float range, float angle, ReceiverInterface receiverInterface) {
        super(parent, frequency, range, receiverInterface, new MultiDetect(parent, new RangeDetect(parent, range), new AngleDetect(parent, angle)));
        this.direction = parent.getDirection();
        this.angle = angle;
    }

    // TODO
    // If parent is not receiverInterface then run time error.
    public AngleRadar(GameObjectInterface parent, float frequency, float range, float angle, Vector3f direction) {
        this(parent, frequency, range, angle, direction, (ReceiverInterface)parent);
    }

    public AngleRadar(MovingObjectInterface parent, float frequency, float range, float angle) {
        this(parent, frequency, range, angle, (ReceiverInterface)parent);
    }

    @Override
    public void illuminate() {
        Environment environment = Environment.getInstance();
        if (parent instanceof MovingObjectInterface m)
            environment.addLaser(new LaserInformation(new PositionInformationImp(parent, parent.getPosition()), frequency, range, m.getDirection(), angle, parent.getTeam().getTeamName(), PaintDrawer.radarColor));
        else
            environment.addLaser(new LaserInformation(new PositionInformationImp(parent, parent.getPosition()), frequency, range, new Vector3f(0, 0, 0), angle, parent.getTeam().getTeamName(), PaintDrawer.radarColor));

        List<GameObject> objects = environment.getObjects(parent.getTeam());

        for(GameObject o: objects)
            if(detectPredicate.test(o))
                Environment.getInstance().addLaser(o.reflect(parent.getPosition(), frequency, range));
    }

    @Override
    public void draw(Graphics2D g2d) {
//        g2d.setColor(radarColor);
//        double angleCos =
//                Math.sqrt((direction.x * direction.x + direction.y * direction.y)
//                        / (direction.x * direction.x + direction.y * direction.y + direction.z * direction.z));
//        double length = range * angleCos;
//
//
//        Vector3f center = parent.getPosition();
//        double centerAngle = GameMath.directionToAngle(new Vector2f(direction.x, direction.y)) % 360;
//
//        g2d.fillArc((int)(center.x - length), (int)(center.y - length), (int)(length * 2), (int)(length * 2), (int)(centerAngle - angle / 2), (int)angle);
    }

    @Override
    public void setParent(GameObject parent) {
        super.setParent(parent);
        if(parent instanceof MovingObjectInterface m)
            direction = m.getDirection();
    }
}
