package aircraftsimulator.GameObject.Aircraft.Communication;

public interface DataReceiver {
    void dataReceived(Object data, String sessionId);
}
