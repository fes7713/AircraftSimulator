package aircraftsimulator.GameObject.Aircraft.Radar;

import aircraftsimulator.Environment;
import aircraftsimulator.GameMath;
import aircraftsimulator.GameObject.Aircraft.Communication.ReceiverInterface;
import aircraftsimulator.GameObject.Aircraft.MovingObjectInterface;
import aircraftsimulator.GameObject.DestructibleObjectInterface;
import aircraftsimulator.GameObject.GameObject;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.List;

public class AngleRadar extends SimpleRadar{

    private Vector3f direction;
    private final float angle;

    public AngleRadar(GameObject parent, float range, float angle, Vector3f direction, ReceiverInterface receiverInterface) {
        super(parent, range, receiverInterface);
        this.direction = direction;
        this.angle = angle;
    }

    // TODO
    // If parent is not receiverInterface then run time error.
    public AngleRadar(GameObject parent, float range, float angle, Vector3f direction) {
        this(parent, range, angle, direction, (ReceiverInterface)parent);
    }

    @Override
    public void detect() {
        List<GameObject> objects = Environment.getInstance().getObjects(parent.getTeam());
        detectedObjects.clear();

        GameObject closestObject = null;

        float rangeSquared = range * range;
        float minLength = Float.MAX_VALUE;

        for(GameObject o: objects)
        {
            if(parent == o || !(o instanceof DestructibleObjectInterface))
                continue;
            Vector3f v = new Vector3f(o.getPosition());
            v.sub(parent.getPosition());
            float lengthSquared = v.lengthSquared();
            if(lengthSquared < rangeSquared)
            {
                float angleCos = direction.dot(v) / direction.length() / v.length();
                if(angleCos > Math.cos(Math.toRadians(angle / 2)))
                {
                    detectedObjects.add(o);
                    if(minLength > lengthSquared)
                    {
                        minLength = lengthSquared;
                        closestObject = o;
                    }
                }
            }
        }
        if(closestObject != null)
            receiverInterface.receive(closestObject.send(detectType()));
        else
            receiverInterface.receive(null);
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
        super.setParent(parent);
    }
}
