package aircraftsimulator.GameObject.Aircraft.Radar.Radar;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;

public interface RadarInterface {
    // Access GamePanel and detect object within a range.
    void illuminate();

    // Set detection filter
    // Detect missile, enemy
    void setFilter();

    Class<? extends Information> detectType();
}
