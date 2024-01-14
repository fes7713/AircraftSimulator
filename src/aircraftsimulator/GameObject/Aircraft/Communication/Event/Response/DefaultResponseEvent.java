package aircraftsimulator.GameObject.Aircraft.Communication.Event.Response;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.BasicEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Event;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.EventPriority;

public class DefaultResponseEvent extends ResponseEvent<Boolean> {
    public DefaultResponseEvent(Event message, Boolean aTrue) {
        super(message.getPort(), message.getDestinationMAC(), message.getSourceMac(), aTrue, EventPriority.MEDIUM);
    }

    public static <E> BasicEvent<Boolean> SuccessResponse(BasicEvent<E> message)
    {
        return new DefaultResponseEvent(message, Boolean.TRUE);
    }

    public static <E> BasicEvent<Boolean> FailResponse(BasicEvent<E> message)
    {
        return new DefaultResponseEvent(message, Boolean.FALSE);
    }
}
