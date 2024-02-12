package aircraftsimulator.GameObject.Aircraft.Communication.Event.Request;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.EventPriority;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.NetworkInterface;

public class PingEvent extends RequestEvent<Long> {
    public PingEvent(int port) {
        super(port, System.currentTimeMillis());
    }

    public PingEvent(int port, NetworkInterface sourceNetworkInterface) {
        super(port, sourceNetworkInterface.getMac(), null, System.currentTimeMillis(), EventPriority.MEDIUM);
    }
}
