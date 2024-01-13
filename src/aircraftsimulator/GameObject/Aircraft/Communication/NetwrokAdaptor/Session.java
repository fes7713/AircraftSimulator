package aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor;

public interface Session {
    int getSessionId();
    void updateTimeout(float delta);
    float getRemainingTimeout();
    boolean isActive();
}
