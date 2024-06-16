package aircraftsimulator.GameObject.Aircraft.Communication;

public interface DataReceiver<E> {
    void dataReceived(E data, String sessionId);
}
