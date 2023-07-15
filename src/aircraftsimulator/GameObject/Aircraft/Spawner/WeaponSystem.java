package aircraftsimulator.GameObject.Aircraft.Spawner;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformation;

public interface WeaponSystem {
    float getRange();
    void fire(PositionInformation fireInformation);
    boolean isAvailable();
}
