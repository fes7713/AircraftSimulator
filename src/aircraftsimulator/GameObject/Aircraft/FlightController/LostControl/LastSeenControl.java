package aircraftsimulator.GameObject.Aircraft.FlightController.LostControl;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformationImp;
import aircraftsimulator.GameObject.DestructibleObjectInterface;

public class LastSeenControl implements LostControlInterface{
    protected Information lastSeenInformation;
    private float lostTime;

    public LastSeenControl()
    {
        setInformation(null);
    }

    @Override
    public void update(float delta)
    {
        if(lastSeenInformation == null)
            return;
        if(lastSeenInformation.getSource() instanceof DestructibleObjectInterface obj)
        {
            if(!obj.isAlive())
            {
                disable();
                return;
            }

        }
        lostTime += delta;
    }

    @Override
    public void setInformation(Information information)
    {
        lastSeenInformation = information;
        lostTime = 0;
    }

    @Override
    public Information getTarget() {
        if(lastSeenInformation instanceof PositionInformation p)
            return new PositionInformationImp(p);
        return lastSeenInformation;
    }

    @Override
    public float getLostTime()
    {
        return lostTime;
    }

    @Override
    public void disable() {
        setInformation(null);
    }

    @Override
    public boolean isLost() {
        return lastSeenInformation != null;
    }
}
