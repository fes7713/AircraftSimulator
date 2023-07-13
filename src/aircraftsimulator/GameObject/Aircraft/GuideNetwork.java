package aircraftsimulator.GameObject.Aircraft;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformation;

public interface GuideNetwork {
    void addToGuidNetwork(Guided guidedObject, PositionInformation keyInformation);
}
