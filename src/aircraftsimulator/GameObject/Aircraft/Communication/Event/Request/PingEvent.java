package aircraftsimulator.GameObject.Aircraft.Communication.Event.Request;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.EventPriority;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Response.PingResponseEvent;

public class PingEvent extends RequestEvent<Long> {
    private final PingEvent wrappingEvent;

    public PingEvent(int port, String sourceMap) {
        super(port, sourceMap, null, System.currentTimeMillis(), EventPriority.MEDIUM);
        wrappingEvent = null;
    }

    public PingEvent(int port, PingEvent pingEvent) {
        super(port, System.currentTimeMillis());
        wrappingEvent = pingEvent;
    }

    public String getSourceMac()
    {
        if(wrappingEvent != null)
            return wrappingEvent.getSourceMac();
        return super.getSourceMac();
    }

    public PingResponseEvent createReply(String sourceMac)
    {
        return new PingResponseEvent(getPort(), sourceMac, getSourceMac(), getData(), "No");
    }
}
