package aircraftsimulator.GameObject.Aircraft.Communication;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformation;

public interface SenderInterface {
    <T extends PositionInformation> PositionInformation send(Class<T> type);
}
