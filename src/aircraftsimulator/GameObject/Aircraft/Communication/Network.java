package aircraftsimulator.GameObject.Aircraft.Communication;

public interface Network {
    void addToNetwork(NetworkComponent networkComponent);
    void removeFromNetwork(NetworkComponent networkComponent);
    void sendTo(Packet packet);
    int getFrameSize();
    void update(float delta);
}
