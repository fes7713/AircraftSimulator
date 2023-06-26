package aircraftsimulator.GameObject.Aircraft.Radar;

import aircraftsimulator.GameObject.Aircraft.Communication.Information.Information;

public interface RadarInterface {
    // Access GamePanel and detect object within a range.
    void detect();

    // Set detection filter
    // Detect missile, enemy
    void setFilter();

    Class<? extends Information> detectType();
}
