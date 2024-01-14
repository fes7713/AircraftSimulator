package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.Event;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.DataProcessor;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.NetworkAdaptor;
import aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor.NetworkInterface;

public interface Router extends NetworkAdaptor, NetworkInterface, DataProcessor {

    void addRouting(int port, NetworkAdaptor component);

    void removeRouting(NetworkAdaptor component);

    void addEvent(Event<?> event);

    void update(float delta);

    int askForPort(String destinationMac);

    void ping();
}
