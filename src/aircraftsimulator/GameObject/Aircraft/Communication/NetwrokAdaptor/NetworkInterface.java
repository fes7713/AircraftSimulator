package aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.Event;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.EventPriority;
import aircraftsimulator.GameObject.Aircraft.Communication.Router;

import java.util.UUID;

public interface NetworkInterface {
    String getName();
    boolean update(float delta);
    default <E> void  sendData(int port, E data){
        sendData(port, data, EventPriority.MEDIUM);
    }
    <E> void sendData(int port, E data, EventPriority priority);
    <E> void receiveData(Event<E> event);
    void setRouter(Router router);
    Router getRouter();
    String getMac();
    Event popData();
    static String generateMAC()
    {
        return UUID.randomUUID().toString();
    }

    default void disconnect() {
        Router router = getRouter();
        if(router != null)
            router.removeRouting(this);
    }
}
