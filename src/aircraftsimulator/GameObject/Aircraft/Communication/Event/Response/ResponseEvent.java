package aircraftsimulator.GameObject.Aircraft.Communication.Event.Response;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.BasicEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.EventPriority;

public class ResponseEvent<E> extends BasicEvent<E> {
    public ResponseEvent(int port, E data) {
        super(port, data);
    }

    public ResponseEvent(int port, E data, EventPriority eventPriority) {
        super(port, data, eventPriority);
    }

    public ResponseEvent(int port, String destinationMac, E data, EventPriority eventPriority) {
        super(port, destinationMac, data, eventPriority);
    }

    public ResponseEvent(int port, String sourceMac, String destinationMac, E data, EventPriority eventPriority) {
        super(port, sourceMac, destinationMac, data, eventPriority);
    }
}
