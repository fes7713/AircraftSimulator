package aircraftsimulator.GameObject.Aircraft.Communication.Event;

public class BasicEvent<E> implements Event<E> {
    private final E data;
    private final EventPriority eventPriority;
    private final int port;
    private final String sourceMac;
    private final String destinationMac;

    public BasicEvent(int port, E data)
    {
        this(port, data, EventPriority.MEDIUM);
    }

    public BasicEvent(int port, E data, EventPriority eventPriority)
    {
        this(port, null, null, data, eventPriority);
    }

    // No need for reply
    // Used for reply message
    public BasicEvent(int port, String destinationMac, E data, EventPriority eventPriority){
        this(port, null, destinationMac, data, eventPriority);
    }

    public BasicEvent(int port, String sourceMac, String destinationMac, E data, EventPriority eventPriority){
        this.port = port;
        this.sourceMac = sourceMac;
        this.destinationMac = destinationMac;
        this.data = data;
        this.eventPriority = eventPriority;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public E getData() {
        return data;
    }

    @Override
    public EventPriority getPriority() {
        return eventPriority;
    }

    @Override
    public String getDestinationMAC() {
        return destinationMac;
    }

    @Override
    public String getSourceMac() {
        return sourceMac;
    }


}
