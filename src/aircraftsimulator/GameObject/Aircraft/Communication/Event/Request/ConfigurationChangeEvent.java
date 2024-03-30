package aircraftsimulator.GameObject.Aircraft.Communication.Event.Request;

import aircraftsimulator.GameObject.Aircraft.Communication.Event.EventPriority;

public class ConfigurationChangeEvent extends RequestEvent<Long> {

    public ConfigurationChangeEvent(int port, String sourceMac) {
        super(port, sourceMac, null, System.currentTimeMillis(), EventPriority.MEDIUM);
    }
}