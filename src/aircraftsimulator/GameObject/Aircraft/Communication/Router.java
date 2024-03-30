package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.Event;
import aircraftsimulator.GameObject.Aircraft.Communication.Event.Request.PingEvent;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.DataProcessor;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.NetworkAdaptor;

public interface Router extends NetworkAdaptor, DataProcessor {

    void addRouting(int port, NetworkAdaptor component);

    void removeRouting(int port);

    boolean update(float delta);

    int askForPort(String destinationMac);

    default void ping(){
        ping(null);
    }

    void ping(PingEvent parentPing);

    void setParentRouter(Router router);

    void dispatchEvent(Event event);
}
