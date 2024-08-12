package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Handler.ConnectionEstablishedHandler;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.NetworkError.NetworkErrorType;
import aircraftsimulator.GameObject.Aircraft.Communication.Timeout.TimeoutInformation;

import java.io.Serializable;
import java.util.function.Consumer;

public interface NetworkComponent extends DataReceiverSwitcher{
    String getMac();
    void update(float delta);
    boolean openPort(int port);
    void enabledPortTransfer(int port);
    void closePort(int port);

    boolean isConnected(int port);
    void connect(int port);
    void connect(int sourcePort, ConnectionEstablishedHandler handler);
    void connect(int sourcePort, int destinationPort, ConnectionEstablishedHandler handler);
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

    void registerTimeout(int port, long timeout, Consumer<Integer> handler);
    void updateTimeout(int port, long timeout);
    void removeTimeout(int port);

    void registerTimeout(String key, long timeout, Consumer<String> handler);
    void registerTimeout(String key, TimeoutInformation information);
    void updateTimeout(String key, long timeout);
    void removeTimeout(String key);
}
