package aircraftsimulator.GameObject.Aircraft.Thruster;

public enum ThrusterLevel {

    STOPPED(0),
    SLOW(25),
    NORMAL(50),
    FAST(75),
    MAX(100);

    private final int percentage;

    ThrusterLevel(int percentage) {
        this.percentage = percentage;
    }

    public int getPercentage() { return percentage; }
}
