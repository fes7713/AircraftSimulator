package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;

public interface SenderInterface {
    <T extends Information> Information send(Class<T> type);
}
