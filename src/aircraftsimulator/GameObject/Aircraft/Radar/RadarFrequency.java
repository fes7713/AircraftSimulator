package aircraftsimulator.GameObject.Aircraft.Radar;

public class RadarFrequency {
    public static final float MF = 0.0003F * 100; // 300kHz
    public static final float HF = 0.003F * 100;
    public static final float VHF = 0.03F * 100;
    public static final float UHF = 0.3F * 100; //300 MHz
    public static final float P = 0.5F * 100;
    public static final float L = 1F * 100;
    public static final float S = 2F * 100; // 2 GHz
    public static final float C = 4F * 100;
    public static final float X = 8F * 100;
    public static final float Ku = 12F * 100; // 12 GHz
    public static final float K = 18F * 100;
    public static final float Ka = 26F * 100;
    public static final float V = 40F * 100;
    public static final float W = 75F * 100;  // 75 GHz


    public static final float MIN = MF;
    public static final float MAX = W;
}
