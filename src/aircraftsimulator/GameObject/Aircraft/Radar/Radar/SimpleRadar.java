package aircraftsimulator.GameObject.Aircraft.Radar.Radar;

import aircraftsimulator.Environment;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Event;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.LaserInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.MotionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformationImp;
import aircraftsimulator.GameObject.Aircraft.Communication.LocalRouter;
import aircraftsimulator.GameObject.Aircraft.Communication.PortEnum;
import aircraftsimulator.GameObject.Aircraft.Communication.ReceiverInterface;
import aircraftsimulator.GameObject.Aircraft.MovingObjectInterface;
import aircraftsimulator.GameObject.Aircraft.Radar.DetectPredicate.DetectPredicate;
import aircraftsimulator.GameObject.Aircraft.Radar.DetectPredicate.RangeDetect;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.GameObject;
import aircraftsimulator.GameObject.GameObjectInterface;
import aircraftsimulator.PaintDrawer;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleRadar extends Component implements RadarInterface{
//    private final Environment environment;
    protected GameObjectInterface parent;
    protected final float power;
    protected final float frequency;

    // TODO delete later
    protected final float range;

    protected List<GameObjectInterface> detectedObjects;
    protected ReceiverInterface receiverInterface;
    protected DetectPredicate detectPredicate;


    public SimpleRadar(GameObjectInterface parent, float frequency, float range, ReceiverInterface receiverInterface)
    {
        this(parent, frequency, range, receiverInterface, new RangeDetect(parent, range));
    }

    public SimpleRadar(GameObjectInterface parent, float frequency, float power, ReceiverInterface receiverInterface, DetectPredicate detectPredicate)
    {
        this.parent = parent;
        this.frequency = frequency;
        this.power = power;
        detectedObjects = new ArrayList<>();
        this.receiverInterface = receiverInterface;
        this.detectPredicate = detectPredicate;

        range = power;
    }

    @Override
    public void illuminate() {
        Environment environment = Environment.getInstance();
        if(parent instanceof MovingObjectInterface m)
            environment.addLaser(new LaserInformation(new PositionInformationImp(parent, parent.getPosition()), frequency, range, m.getDirection(), 360, parent.getTeam().getTeamName(), PaintDrawer.radarColor));
        else
            environment.addLaser(new LaserInformation(new PositionInformationImp(parent, parent.getPosition()), frequency, range, new Vector3f(0, 0, 0), 360, parent.getTeam().getTeamName(), PaintDrawer.radarColor));
        List<GameObject> objects = environment.getObjects(parent.getTeam());
//        detectedObjects.clear();
//        boolean detected = false;
        for(GameObject o: objects)
        {
//            if(parent == o || !(o instanceof DestructibleObjectInterface))
//                continue;
            if(detectPredicate.test(o))
            {
//                receiverInterface.receive(info);

                // TODO chnage range
                Environment.getInstance().addLaser(o.reflect(parent.getPosition(), frequency, range));
//                detected = true;
            }
        }
//        if(!detected)
//            receiverInterface.receive(null);


    }

    @Override
    public void setFilter() {

    }

    @Override
    public Class<? extends PositionInformation> detectType() {
        return MotionInformation.class;
    }

    // TODO May cause bug here Parent does not match or already defined
    @Override
    public void setParent(GameObject parent) {
        this.parent = parent;
        detectPredicate.setParent(parent);
        if(parent instanceof ReceiverInterface receiver)
            receiverInterface = receiver;
        else
            receiverInterface = (v) -> {};
    }

    @Override
    public void update(float delta) {
        illuminate();
    }

    @Override
    public void draw(Graphics2D g2d) {
//        Vector3f center = parent.getPosition();
//        g2d.setColor(radarColor);
//
//        // TODO power is not equal to the range
//        float range = power;
//        g2d.fillOval((int)(center.x - range), (int)(center.y - range), (int)range * 2, (int)range * 2);
    }

    @Override
    public SimpleRadar clone() {
        SimpleRadar clone = (SimpleRadar) super.clone();
        clone.detectedObjects = new ArrayList<>();
        clone.detectPredicate = detectPredicate.copy();
        return clone;
    }


    @Override
    public void receiveEvent(Event e) {

    }

    @Override
    public void addToRouter(LocalRouter router) {
        router.addRouting(PortEnum.RADAR, this);
    }
}
