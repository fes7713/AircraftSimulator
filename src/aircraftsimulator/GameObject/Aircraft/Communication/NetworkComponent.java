package aircraftsimulator.GameObject.Aircraft.Communication;

public interface NetworkComponent {
    String getMac();
    void update(float delta);
    boolean openPort(int port);
    void closePort(int port);

    boolean isConnected(int port);
    void connect(int port);
//    void handshake(boolean syn, boolean ack, boolean fin, NetworkComponent connectingComponent);
//    default void disconnect(int port, NetworkComponent connectingComponent){
//        handshake(false, false, true, connectingComponent);
//    }
    void send(Packet packet);
    void receive(Packet packet);
}
