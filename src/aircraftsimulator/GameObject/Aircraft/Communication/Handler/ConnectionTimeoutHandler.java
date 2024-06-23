package aircraftsimulator.GameObject.Aircraft.Communication.Handler;

public interface ConnectionTimeoutHandler extends Handler {
    void handleConnectionTimeout(String sessionId, Integer retryNum);
    void handleConnectionEstablished(String sessionId, Integer port);
}
