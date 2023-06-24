package aircraftsimulator.GameObject.Aircraft.Radar;

import aircraftsimulator.Environment;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.GameObject;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleRadar extends Component implements RadarInterface{
    private final Environment environment;
    private final GameObject parent;
    private final float range;
    private final List<GameObject> detectedObjects;
    private final RadarSignal radarSignal;

    public static Color radarColor = new Color(71,179,77);

    public SimpleRadar(GameObject parent, float range, RadarSignal signal)
    {
        environment = Environment.getInstance();
        this.parent = parent;
        this.range = range;
        detectedObjects = new ArrayList<>();
        radarSignal = signal;
    }

    @Override
    public void detect() {
        List<GameObject> objects = environment.getObjects();
        detectedObjects.clear();
        float rangeSquared = range * range;
        float minLength = Float.MAX_VALUE;
        GameObject closestObject = null;
        for(GameObject o: objects)
        {
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
        radarSignal.process(closestObject);
    }

    @Override
    public void setFilter() {

    }

    @Override
    public void draw(Graphics2D g2d) {
        Vector3f center = parent.getPosition();
        g2d.setColor(radarColor);
        g2d.fillOval((int)(center.x - range), (int)(center.y - range), (int)range, (int)range);
    }
}
