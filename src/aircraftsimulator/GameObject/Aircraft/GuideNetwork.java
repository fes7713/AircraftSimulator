package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformation;

public interface GuideNetwork {
    void connectToGuidance(Guided guidedObject, PositionInformation keyInformation);
    void disconnectFromGuidance(Guided guidedObject);
}
