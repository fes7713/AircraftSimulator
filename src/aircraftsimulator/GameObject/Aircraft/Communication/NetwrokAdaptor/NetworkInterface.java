package aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.Event;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.EventPriority;
import aircraftsimulator.GameObject.Aircraft.Communication.Router;

import java.util.UUID;

public interface NetworkInterface {
    boolean update(float delta);
    default <E> void  sendData(int port, E data){
        sendData(port, data, EventPriority.MEDIUM);
    }
    <E> void sendData(int port, E data, EventPriority priority);
    <E> void receiveData(Event<E> event);
    void setRouter(Router router);
    Router getRouter();
    String getMac();
    float getProcessTime();
    static String generateMAC()
    {
        return UUID.randomUUID().toString();
    }
}
