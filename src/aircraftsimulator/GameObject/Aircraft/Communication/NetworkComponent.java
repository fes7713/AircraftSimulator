package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.NetworkError.NetworkErrorType;

import java.io.Serializable;

public interface NetworkComponent extends DataReceiverSwitcher{
    String getMac();
    void update(float delta);
    boolean openPort(int port);
    void closePort(int port);

    boolean isConnected(int port);
    void connect(int port);
    void connect(int sourcePort, int destinationPort);
    void disconnect(Integer port);
    void disconnect(String sessionId);
    void releasePort(int port);
    void send(Packet packet);
    void receive(Packet packet);
    void sendData(Integer port, Serializable data);

    <E extends Serializable> void addDataReceiver(Class<E> cls, DataReceiver<E> dataReceiver);
    void errorHandler(String sessionId, NetworkErrorType type);
    void errorHandler(int port, NetworkErrorType type);
}
