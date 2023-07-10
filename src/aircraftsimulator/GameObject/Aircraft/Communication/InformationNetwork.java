package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import aircraftsimulator.GameObject.DestructibleObjectInterface;
import aircraftsimulator.GameObject.GameObject;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class InformationNetwork implements ReceiverInterface {
    private final GameObject parent;
    private final List<ReceiverInterface> receivers;

    public InformationNetwork(GameObject parent)
    {
        this.parent = parent;
        receivers = new ArrayList<>();
    }

    public void addReceiver(ReceiverInterface receiverInterface)
    {
        receivers.add(receiverInterface);
    }

    public void removeReceiver(ReceiverInterface receiverInterface)
    {
        receivers.remove(receiverInterface);
    }

    @Override
    public void receive(@Nullable Information information) {
        for(ReceiverInterface r: receivers)
            r.receive(information);
    }

    public void update()
    {
        for(int i = 0; i < receivers.size(); i++)
        {
            if(receivers.get(i) instanceof DestructibleObjectInterface d)
                if(!d.isAlive())
                    removeReceiver((ReceiverInterface) d);
        }
    }

    public void draw(Graphics2D g2d)
    {
        for(ReceiverInterface r : receivers)
        {
            if(r instanceof GameObject o)
            {
                Vector3f p1 = parent.getPosition();
                Vector3f p2 = o.getPosition();
                g2d.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
            }
        }
    }
}
