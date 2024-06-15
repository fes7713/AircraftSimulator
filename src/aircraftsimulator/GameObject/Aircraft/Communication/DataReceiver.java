package aircraftsimulator.GameObject.Aircraft.Communication;

import java.io.Serializable;

public interface DataReceiver {
    void dataReceived(Serializable data);
}
