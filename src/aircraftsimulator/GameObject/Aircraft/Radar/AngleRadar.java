package aircraftsimulator.GameObject.Aircraft.Radar;

import aircraftsimulator.Environment;
import aircraftsimulator.GameMath;
import aircraftsimulator.GameObject.Aircraft.Communication.ReceiverInterface;
import aircraftsimulator.GameObject.GameObject;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.List;

public class AngleRadar extends SimpleRadar{

    private final AngleRadarDirectionInterface directionInterface;
    private final float angle;
    private GameObject closestObject;
    private float minLength;

    public AngleRadar(GameObject parent, float range, float angle, AngleRadarDirectionInterface directionInterface, ReceiverInterface receiverInterface) {
        super(parent, range, receiverInterface);
        this.directionInterface = directionInterface;
        this.angle = angle;
    }

    @Override
    public void detect() {
        Environment environment = Environment.getInstance();
        List<GameObject> objects = environment.getObjects();
        Vector3f direction = directionInterface.getDirection();

        detectedObjects.clear();
        float rangeSquared = range * range;

        closestObject = null;
        minLength = Float.MAX_VALUE;

        for(GameObject o: objects)
        {
            if(parent == o)
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
        Vector3f d = directionInterface.getDirection();
        double angleCos = Math.sqrt((d.x * d.x + d.y * d.y) / (d.x * d.x + d.y * d.y + d.z * d.z));
        double length = range * angleCos;


        Vector3f center = parent.getPosition();
        double centerAngle = GameMath.directionToAngle(new Vector2f(d.x, d.y)) % 360;

        g2d.fillArc((int)(center.x - length), (int)(center.y - length), (int)(length * 2), (int)(length * 2), (int)(centerAngle - angle / 2), (int)angle);
    }
}
