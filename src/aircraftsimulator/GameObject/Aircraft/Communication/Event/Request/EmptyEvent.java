package aircraftsimulator.GameObject.Aircraft.Communication.Event.Request;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.EventPriority;

public class EmptyEvent extends RequestEvent<Integer>{
    public EmptyEvent(int port) {
        super(port, 0);
    }

    public EmptyEvent(int port, String destinationMac, EventPriority eventPriority) {
        super(port, destinationMac, 0, eventPriority);
    }

    public EmptyEvent(int port, String sourceMac, String destinationMac, EventPriority eventPriority) {
        super(port, sourceMac, destinationMac, 0, eventPriority);
    }
}
