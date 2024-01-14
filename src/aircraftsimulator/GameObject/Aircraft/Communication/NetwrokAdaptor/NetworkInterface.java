package aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.Event;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.EventPriority;
import aircraftsimulator.GameObject.Aircraft.Communication.Router;

import java.util.UUID;

public interface NetworkInterface {
    void update(float delta);
    default <E> void  sendData(int port, E data){
        sendData(port, data, EventPriority.MEDIUM);
    }
    <E> void sendData(int port, E data, EventPriority priority);
    <E> void receiveData(Event<E> event);
    void setRouter(Router router);
    String getMac();
    float getProcessTime();
    NetworkInterfaceMode getNetworkMode();
    static String generateMAC()
    {
        return UUID.randomUUID().toString();
    }
}
