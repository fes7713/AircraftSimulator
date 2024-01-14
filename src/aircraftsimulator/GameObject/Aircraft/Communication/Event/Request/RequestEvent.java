package aircraftsimulator.GameObject.Aircraft.Communication.Event.Request;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.BasicEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.EventPriority;

public class RequestEvent<E> extends BasicEvent<E> {
    public RequestEvent(int port, E data) {
        super(port, data);
    }

    public RequestEvent(int port, E data, EventPriority eventPriority) {
        super(port, data, eventPriority);
    }

    public RequestEvent(int port, String destinationMac, E data, EventPriority eventPriority) {
        super(port, destinationMac, data, eventPriority);
    }

    public RequestEvent(int port, String sourceMac, String destinationMac, E data, EventPriority eventPriority) {
        super(port, sourceMac, destinationMac, data, eventPriority);
    }
}
