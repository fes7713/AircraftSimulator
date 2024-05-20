package aircraftsimulator.GameObject.Aircraft.Communication;

public interface NetworkComponent {
    String getMac();
    void update(float delta);
    boolean openPort(int port);
    void closePort(int port);

    boolean isConnected(int port);
    void connect(int port);
    void disconnect(Integer port);
    void send(Packet<?> packet);
    void receive(Packet<?> packet);
    void sendData(String sessionId, Object data);
}
