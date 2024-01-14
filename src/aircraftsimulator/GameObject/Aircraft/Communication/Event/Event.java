package aircraftsimulator.GameObject.Aircraft.Communication.Event;

public interface Event<T> {
    int getPort();
    T getData();
    EventPriority getPriority();
    String getDestinationMAC();
    String getSourceMac();
}
