package aircraftsimulator.GameObject.Aircraft.Radar.Detector;

import aircraftsimulator.Environment;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.LaserInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.ReceiverInterface;
import aircraftsimulator.GameObject.Aircraft.Radar.DetectPredicate.DetectPredicate;
import aircraftsimulator.GameObject.Aircraft.Radar.DetectPredicate.RangeDetect;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.GameObject;
import aircraftsimulator.GameObject.GameObjectInterface;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleDetector extends Component implements DetectorInterface {

    protected GameObjectInterface parent;
    protected final float range;
    protected final float frequency;

    protected List<GameObject> detectedObjects;
    protected ReceiverInterface receiverInterface;
    protected DetectPredicate detectPredicate;

    public static Color detectorColor = new Color(0,0,0, 20);

    public SimpleDetector(GameObjectInterface parent, float frequency, float range, ReceiverInterface receiverInterface)
    {
        this(parent, frequency, range, receiverInterface, new RangeDetect(parent, range));
    }

    public SimpleDetector(GameObjectInterface parent, float frequency, float range, ReceiverInterface receiverInterface, DetectPredicate detectPredicate)
    {
        this.parent = parent;
        this.frequency = frequency;
        this.range = range;
        detectedObjects = new ArrayList<>();
        this.receiverInterface = receiverInterface;
        this.detectPredicate = detectPredicate;
    }

    @Override
    public void detect() {
        Environment environment = Environment.getInstance();
        List<LaserInformation> lasers = environment.getLasers(frequency);
        detectedObjects.clear();
        boolean detected = false;
        for(LaserInformation laser: lasers)
        {
            if(parent == laser.getSource())
                continue;

            // IFF here
            if(laser.getCode().equals(parent.getTeam().getTeamName()))
                continue;

            if(detectPredicate.test(laser))
            {
                receiverInterface.receive(laser.getInformation());
                detected = true;
            }
        }
        if(!detected)
            receiverInterface.receive(null);
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
        g2d.setColor(detectorColor);
        g2d.fillOval((int)(center.x - range), (int)(center.y - range), (int)range * 2, (int)range * 2);
    }

    @Override
    public SimpleDetector clone() {
        SimpleDetector clone = (SimpleDetector) super.clone();
        clone.detectedObjects = new ArrayList<>();
        clone.detectPredicate = detectPredicate.copy();
        return clone;
    }
}
