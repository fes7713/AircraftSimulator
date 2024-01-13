package aircraftsimulator.GameObject.Aircraft.Communication.Event;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.LaserInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.Session;

public class AttackEvent extends BasicEvent<LaserInformation> {

    public AttackEvent(Session session, LaserInformation laserInformation)
    {
        super(session, laserInformation, EventPriority.MEDIUM);
    }

    public AttackEvent(Session session, LaserInformation laserInformation, EventPriority eventPriority)
    {
        super(session, laserInformation, eventPriority);
    }
}
