package aircraftsimulator.GameObject.Aircraft.Radar.Radar;

import aircraftsimulator.Environment;
import aircraftsimulator.GameMath;
import aircraftsimulator.GameObject.Aircraft.Communication.Network;
import aircraftsimulator.GameObject.Aircraft.Radar.Wave.ElectroMagneticWave;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.GameObject;
import aircraftsimulator.PaintDrawer;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.awt.*;

public abstract class RadioEmitter extends Component implements RadarInterface{
    protected GameObject parent;
    protected Vector3f direction;
    protected final float power;
    protected final float frequency;
    protected final String code;

    protected final float beamAngle;
    protected final float radarMaxAngle;
    protected float radarHorizontalAngle;
    protected float radarVerticalAngle;

    protected final float gain;
    protected final Color color;

    protected final double minimumDetectionIntensity;
    protected final double maxWaveRange;
    protected final double maxDetectionRange;

    protected boolean active;

    private final double DEFAULT_RCS = 5 * 5 * Math.PI * 1000;

    public RadioEmitter(GameObject parent, Network network, float frequency, float power, float radarMaxAngle, float antennaDiameter, float detectionSNR) {
        active = false;

        this.frequency = frequency;
        this.power = power;
        this.parent = parent;
        direction = new Vector3f(parent.getDirection());
        code = parent.getTeam().getPW();

        beamAngle = AngleFromArea(antennaDiameter, frequency);

        this.radarMaxAngle = radarMaxAngle;
        setVerticalAngle(0);
        setHorizontalAngle(0);

        gain = calcGain();
        color = ElectroMagneticWave.GenerateFrequencyColor(frequency);
        System.out.println(calcEffectiveArea());
        minimumDetectionIntensity = Math.pow(10, detectionSNR / 10) * Environment.ENVIRONMENTAL_WAVE;

        maxWaveRange = getMaxWaveRange(Environment.ENVIRONMENTAL_WAVE);
        maxDetectionRange = getMaxDetectionRange(DEFAULT_RCS, minimumDetectionIntensity);
    }

    private float calcGain()
    {
        return 52525F / beamAngle / beamAngle;
    }

    private double calcEffectiveArea()
    {
        return gain * ElectroMagneticWave.LIGHT_SPEED * ElectroMagneticWave.LIGHT_SPEED / (4 * Math.PI * frequency * frequency);
    }

    protected void detect() {

    }

    @Override
    public void illuminate() {
        if(!active)
            return;
        Vector3f left = new Vector3f(- direction.y, direction.x, 0);
        Vector3f up = GameMath.rotateUp90(direction);
        Vector3f newDirection = GameMath.rotatedDirection(Math.toRadians(radarHorizontalAngle), direction, left);
        Vector3f newDirection1 = GameMath.rotatedDirection(Math.toRadians(radarVerticalAngle), newDirection, up);

        Environment.getInstance().addPulseWave(new ElectroMagneticWave(parent, parent.getPosition(), power * gain, frequency,  newDirection1, beamAngle));
    }

    @Override
    public double getMaxWaveRange(double minIntensity) {
        return Math.sqrt(power * gain / (4 * Math.PI * minIntensity));
    }

    @Override
    public double getMaxDetectionRange(double rcs, double minimumDetectionPower) {
        return Math.pow((power * gain * rcs) / (16 * Math.PI * Math.PI * minimumDetectionPower), 0.25);
    }

    @Override
    public boolean setHorizontalAngle(float angle) {
        radarHorizontalAngle = Math.min(Math.max(angle, - radarMaxAngle / 2 + beamAngle / 2), radarMaxAngle / 2 - beamAngle / 2);
        return angle == radarHorizontalAngle;
    }

    @Override
    public boolean setVerticalAngle(float angle) {
        radarVerticalAngle = Math.min(Math.max(angle, - radarMaxAngle / 2 + beamAngle / 2), radarMaxAngle / 2 - beamAngle / 2);
        return angle == radarVerticalAngle;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void update(float delta) {
        direction.set(parent.getDirection());
        if(active)
            detect();
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(PaintDrawer.opacColor(color, 0.3F));
        Vector3f position = parent.getPosition();
        double horizontalCos = GameMath.getCosAngleToHorizontal(direction);
        Vector3f up = GameMath.rotateUp90(direction);
        Vector3f radarDirection = GameMath.rotatedDirection(Math.toRadians(radarVerticalAngle), direction, up);
        double horizontalRadarCos = GameMath.getCosAngleToHorizontal(radarDirection);
        double radarCenterAngle = (GameMath.directionToAngle(new Vector2f(direction.x, direction.y)) - radarHorizontalAngle) % 360;

        double radarLeftAngle = (GameMath.directionToAngle(new Vector2f(direction.x, direction.y)) - radarMaxAngle / 2) % 360;

        double rangeWaveOnScreen = horizontalCos * maxWaveRange;
        double rangeMaxDetection = horizontalCos * maxDetectionRange;
        double rangeRange = horizontalRadarCos * maxDetectionRange;

        GameMath.drawArc(g2d, position, rangeMaxDetection,(int) radarLeftAngle, (int)radarMaxAngle);
        if(active)
            g2d.fillArc((int)(position.x - rangeRange), (int)(position.y - rangeRange), (int)(rangeRange * 2), (int)(rangeRange * 2), (int)(radarCenterAngle - beamAngle / 2), (int) beamAngle);
    }

    @Override
    public void setParent(GameObject parent) {
        this.parent = parent;

    }

    @Override
    public void setup() {
        super.setup();

    }

    public static float AngleFromArea(float parabolicDiameter, float frequency)
    {
        return 70 * ElectroMagneticWave.LIGHT_SPEED / parabolicDiameter / frequency;
    }
}
