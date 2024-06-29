package aircraftsimulator.GameObject.Aircraft.Communication.Handler;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.Data;
import aircraftsimulator.GameObject.Aircraft.Communication.DataReceiver;
import aircraftsimulator.GameObject.Aircraft.Communication.Handler.NetworkError.NetworkErrorType;

import java.io.Serializable;

public interface FragmentAdaptor {
    <E extends Data> void  serializableDataSend(String sessionId, E data);
    int askForWindowSize();
    <E extends Serializable> void addDataReceiver(Class<E> cls, DataReceiver<E> dataReceiver);

    void triggerReceiver(String sessionId, Object object);
    void errorHandler(String sessionId, NetworkErrorType type);
    void sendCompletionHandler(String sessionId);

    int getPortNumber(String sessionId);
}
