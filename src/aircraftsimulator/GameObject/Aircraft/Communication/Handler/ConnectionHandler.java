package aircraftsimulator.GameObject.Aircraft.Communication.Handler;

public interface ConnectionHandler extends Handler {
    void handleConnectionTimeout(String sessionId, Integer retryNum);
    void handleConnectionEstablished(String sessionId, Integer port);
}
