package aircraftsimulator.GameObject.Aircraft.Radar;

import aircraftsimulator.Environment;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.MotionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.ReceiverInterface;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.GameObject;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleRadar extends Component implements RadarInterface{
//    private final Environment environment;
    protected GameObject parent;
    protected final float range;
    protected final List<GameObject> detectedObjects;
    protected final ReceiverInterface receiverInterface;

    public static Color radarColor = new Color(71,179,77, 100);

    public SimpleRadar(GameObject parent, float range, ReceiverInterface receiverInterface)
    {
        this.parent = parent;
        this.range = range;
        detectedObjects = new ArrayList<>();
        this.receiverInterface = receiverInterface;
    }

    @Override
    public void detect() {
        Environment environment = Environment.getInstance();
        List<GameObject> objects = environment.getObjects();
        detectedObjects.clear();
        float rangeSquared = range * range;
        float minLength = Float.MAX_VALUE;
        GameObject closestObject = null;
        for(GameObject o: objects)
        {
            if(parent == o)
                continue;
            Vector3f v = new Vector3f(parent.getPosition());
            v.sub(o.getPosition());
            float lengthSquared = v.lengthSquared();
            if(lengthSquared < rangeSquared)
            {
                detectedObjects.add(o);
                if(minLength > lengthSquared)
                {
                    minLength = lengthSquared;
                    closestObject = o;
                }
            }
        }
        if(closestObject != null)
            receiverInterface.receive(closestObject.send(detectType()));
        else
            receiverInterface.receive(null);
    }

    @Override
    public void setFilter() {

    }

    @Override
    public Class<? extends Information> detectType() {
        return MotionInformation.class;
    }

    // TODO May cause bug here Parent does not match or already defined
    @Override
    public void setParent(GameObject parent) {
        this.parent = parent;
    }

    @Override
    public void update(float delta) {
        detect();
    }

    @Override
    public void draw(Graphics2D g2d) {
        Vector3f center = parent.getPosition();
        g2d.setColor(radarColor);
        g2d.fillOval((int)(center.x - range), (int)(center.y - range), (int)range * 2, (int)range * 2);
    }


}
