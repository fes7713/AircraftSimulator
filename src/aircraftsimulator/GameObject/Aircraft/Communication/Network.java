package aircraftsimulator.GameObject.Aircraft.Communication;

public interface Network {
    void addToNetwork(NetworkComponent networkComponent);
    void removeFromNetwork(NetworkComponent networkComponent);
    void broadcast(Packet packet, String sourceMac);
    void sendTo(String mac, Packet packet);
}
