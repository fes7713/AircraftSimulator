package aircraftsimulator.GameObject.Aircraft.Radar;

public interface RadarInterface {
    // Access GamePanel and detect object within a range.
    void detect();

    // Set detection filter
    // Detect missile, enemy
    void setFilter();
}
