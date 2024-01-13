package aircraftsimulator.GameObject.Aircraft.Communication.Event;

public interface Event<T> {
    int getPort();
    T getData();
    int getPriority();
    int getSessionId();
}
