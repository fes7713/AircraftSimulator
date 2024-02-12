package aircraftsimulator.GameObject.Aircraft.Communication.Event.Response;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.EventPriority;

public class TextResponseEvent extends ResponseEvent<String> {
    public TextResponseEvent(int port, String destinationMac, String data) {
        super(port, destinationMac, data, EventPriority.LOW);
    }
}
