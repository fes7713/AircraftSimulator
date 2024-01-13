package aircraftsimulator.GameObject.Aircraft.Communication.Event;

import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.Session;

import static aircraftsimulator.GameObject.Aircraft.Communication.PortEnum.ATTACK;

public abstract class BasicEvent<E> implements Event<E> {
    private final int sessionId;
    private final E data;
    private final EventPriority eventPriority;

    public BasicEvent(Session session, E data)
    {
        this(session, data, EventPriority.MEDIUM);
    }

    public BasicEvent(Session session, E data, EventPriority eventPriority)
    {
        this.sessionId = session.getSessionId();
        this.data = data;
        this.eventPriority = eventPriority;
    }

    @Override
    public int getPort() {
        return ATTACK;
    }

    @Override
    public E getData() {
        return data;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getSessionId() {
        return sessionId;
    }
}
