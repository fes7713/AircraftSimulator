package aircraftsimulator.GameObject.Aircraft.Radar;

import aircraftsimulator.Environment;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.MotionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.ReceiverInterface;
import aircraftsimulator.GameObject.Aircraft.Radar.DetectPredicate.DetectPredicate;
import aircraftsimulator.GameObject.Aircraft.Radar.DetectPredicate.RangeDetect;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.DestructibleObjectInterface;
import aircraftsimulator.GameObject.GameObject;
import aircraftsimulator.GameObject.GameObjectInterface;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleRadar extends Component implements RadarInterface{
//    private final Environment environment;
    protected GameObjectInterface parent;
    protected final float range;
    protected List<GameObject> detectedObjects;
    protected ReceiverInterface receiverInterface;
    protected DetectPredicate detectPredicate;

    public static Color radarColor = new Color(71,179,77, 100);

    public SimpleRadar(GameObjectInterface parent, float range, ReceiverInterface receiverInterface)
    {
        this(parent, range, receiverInterface, new RangeDetect(parent, range));
    }

    public SimpleRadar(GameObjectInterface parent, float range, ReceiverInterface receiverInterface, DetectPredicate detectPredicate)
    {
        this.parent = parent;
        this.range = range;
        detectedObjects = new ArrayList<>();
        this.receiverInterface = receiverInterface;
        this.detectPredicate = detectPredicate;
    }

    @Override
    public void detect() {
        Environment environment = Environment.getInstance();
        List<GameObject> objects = environment.getObjects(parent.getTeam());
        detectedObjects.clear();
        boolean detected = false;
        for(GameObject o: objects)
        {
            if(parent == o || !(o instanceof DestructibleObjectInterface))
                continue;
            if(detectPredicate.test(o))
            {
                receiverInterface.receive(o.send(detectType()));
                detected = true;
            }
        }
        if(!detected)
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
        if(parent instanceof ReceiverInterface receiver)
            receiverInterface = receiver;
        else
            receiverInterface = (v) -> {};
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

    @Override
    public SimpleRadar clone() {
        SimpleRadar clone = (SimpleRadar) super.clone();
        clone.detectedObjects = new ArrayList<>();
        return clone;
    }
}
