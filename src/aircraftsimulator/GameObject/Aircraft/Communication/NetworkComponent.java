package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.Data;

import java.io.Serializable;

public interface NetworkComponent {
    String getMac();
    void update(float delta);
    boolean openPort(int port);
    void closePort(int port);

    boolean isConnected(int port);
    void connect(int port);
    void connect(int sourcePort, int destinationPort);
    void disconnect(Integer port);
    void disconnect(String sessionId);
    void send(Packet packet);
    void receive(Packet packet);
    void sendData(Integer port, Serializable data);

    void addDataReceiver(Class<? extends Data>cls, DataReceiver dataReceiver);
}
