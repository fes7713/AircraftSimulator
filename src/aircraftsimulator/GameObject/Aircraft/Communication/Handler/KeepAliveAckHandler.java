package aircraftsimulator.GameObject.Aircraft.Communication.Handler;

public interface KeepAliveAckHandler extends Handler{
    void handleKeepAliveAck(String session, Integer retryNum);
}
