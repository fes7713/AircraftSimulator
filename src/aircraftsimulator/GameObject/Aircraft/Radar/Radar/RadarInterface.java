package aircraftsimulator.GameObject.Aircraft.Radar.Radar;

public interface RadarInterface {
    void illuminate();
    double getMaxWaveRange(double minIntensity);
    double getMaxDetectionRange(double rcs, double minIntensity);
}
