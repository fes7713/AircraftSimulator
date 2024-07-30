package aircraftsimulator.GameObject.Aircraft.Radar;

public class RadarFrequency {
    public static final int MULTIPLIER = 1000;

    public static final float MF = 0.0003F * MULTIPLIER; // 300kHz
    public static final float HF = 0.003F * MULTIPLIER;
    public static final float VHF = 0.03F * MULTIPLIER;
    public static final float UHF = 0.3F * MULTIPLIER; //300 MHz
    public static final float P = 0.5F * MULTIPLIER;
    public static final float L = 1F * MULTIPLIER;
    public static final float S = 2F * MULTIPLIER; // 2 GHz
    public static final float C = 4F * MULTIPLIER;
    public static final float X = 8F * MULTIPLIER;
    public static final float Ku = 12F * MULTIPLIER; // 12 GHz
    public static final float K = 18F * MULTIPLIER;
    public static final float Ka = 26F * MULTIPLIER;
    public static final float V = 40F * MULTIPLIER;
    public static final float W = 75F * MULTIPLIER;  // 75 GHz


    public static final float MIN = MF;
    public static final float MAX = W;
}
