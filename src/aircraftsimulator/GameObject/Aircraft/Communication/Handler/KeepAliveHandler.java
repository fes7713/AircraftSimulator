package aircraftsimulator.GameObject.Aircraft.Communication.Handler;

public interface KeepAliveHandler extends Handler{
    void registerKeepAliveTimeout(String sessionId, long timeout);
    void handleKeepAlive(String sessionId, Integer retryNum);
}
