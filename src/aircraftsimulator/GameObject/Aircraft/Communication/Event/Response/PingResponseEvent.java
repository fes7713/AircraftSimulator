package aircraftsimulator.GameObject.Aircraft.Communication.Event.Response;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.EventPriority;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Request.PingEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.NetworkInterface;

public class PingResponseEvent extends ResponseEvent<Long>{

    public PingResponseEvent(PingEvent receivedPing, int port, NetworkInterface sourceNetworkInterface){
        this(port, sourceNetworkInterface.getMac(), receivedPing.getSourceMac(), receivedPing.getData());
    }

    public PingResponseEvent(int port, String sourceMac, String destinationMac, Long data) {
        super(port, sourceMac, destinationMac, data, EventPriority.MEDIUM);
    }
}
