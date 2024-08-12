package aircraftsimulator.GameObject.Aircraft.Radar.Radar;

public interface RadarInterface {
    void illuminate();
    double getMaxWaveRange(double minIntensity);
    double getMaxDetectionRange(double rcs, double minIntensity);

    boolean setHorizontalAngle(float angle);
    boolean setVerticalAngle(float angle);

    void setActive(boolean active);
}
