package aircraftsimulator.GameObject.Aircraft.Spawner;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.ReceiverInterface;
import aircraftsimulator.GameObject.DestructibleObjectInterface;
import aircraftsimulator.GameObject.GameObject;
import org.jetbrains.annotations.Nullable;

public class TargetTimerSpawner<T extends GameObject> extends TimeSpawner<T> implements ReceiverInterface {
    protected PositionInformation target;

    public TargetTimerSpawner(GameObject parent, float interval) {
        super(parent, interval);
    }

    @Override
    public void receive(@Nullable Information information) {
        if(information == null)
        {
            this.target = null;
            return;
        }
        if(information.getSource() instanceof DestructibleObjectInterface)
            this.target = (PositionInformation) information;
    }

    @Override
    public void spawn(){
        super.spawn();
        resetTarget();
    }

    public void resetTarget()
    {
        this.target = null;
    }
}
