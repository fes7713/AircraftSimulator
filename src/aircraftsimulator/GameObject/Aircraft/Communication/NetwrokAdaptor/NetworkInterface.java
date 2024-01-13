package aircraftsimulator.GameObject.Aircraft.Communication.NetwrokAdaptor;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.Event;

public interface NetworkInterface {
    <E> void  sendData(Event<E> event);
    <E> void receiveData(Event<E> event);
}
