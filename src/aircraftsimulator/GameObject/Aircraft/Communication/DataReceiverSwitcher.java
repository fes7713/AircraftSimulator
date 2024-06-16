package aircraftsimulator.GameObject.Aircraft.Communication;

public interface DataReceiverSwitcher{
    void triggerReceiver(String sessionId, Object object);
}
