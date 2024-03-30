package aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.Event;
import aircraftsimulator.GameObject.Aircraft.Communication.Router;

import java.util.UUID;

public interface NetworkInterface {
    String getName();
    boolean update(float delta);
    <E> void  sendData(Event<E> data);
    <E> void receiveData(Event<E> event);

    void setRouter(Router router);
    Router getRouter();
    String getMac();
    Event popData();
    static String generateMAC()
    {
        return UUID.randomUUID().toString();
    }


}
