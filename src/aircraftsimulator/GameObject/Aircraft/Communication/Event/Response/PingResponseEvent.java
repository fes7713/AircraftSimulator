package aircraftsimulator.GameObject.Aircraft.Communication.Event.Response;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.EventPriority;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Request.PingEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.NetworkInterface;

public class PingResponseEvent extends ResponseEvent<Long>{

    private final String message;

    public PingResponseEvent(PingEvent receivedPing, int port, NetworkInterface sourceNetworkInterface, String message){
        this(port, sourceNetworkInterface.getMac(), receivedPing.getSourceMac(), receivedPing.getData(), message);
    }

    public PingResponseEvent(int port, String sourceMac, String destinationMac, Long data, String message) {
        super(port, sourceMac, destinationMac, data, EventPriority.MEDIUM);
        this.message = message;
    }

    public String getMessage(){
        return message;
    }
}
