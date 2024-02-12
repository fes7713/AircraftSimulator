package aircraftsimulator.GameObject.Aircraft.Communication.Event.Request;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.EventPriority;

public class PingEvent extends RequestEvent<Long> {
    public PingEvent(int port) {
        super(port, System.currentTimeMillis());
    }

    public PingEvent(int port, String sourceMap) {
        super(port, sourceMap, null, System.currentTimeMillis(), EventPriority.MEDIUM);
    }
}
