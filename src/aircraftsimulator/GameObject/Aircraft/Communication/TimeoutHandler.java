package aircraftsimulator.GameObject.Aircraft.Communication;

public interface TimeoutHandler {
    void triggerTimeout(Integer port, SessionInformation sessionInformation);
}
