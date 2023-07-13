package aircraftsimulator.GameObject.Aircraft.Spawner;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.FireInformation;

public interface WeaponSystem {
    float getRange();
    void fire(FireInformation fireInformation);
    boolean isAvailable();
}
