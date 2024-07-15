package aircraftsimulator.GameObject.Aircraft.Radar.Radar;

import aircraftsimulator.Environment;
import aircraftsimulator.GameMath;
import aircraftsimulator.GameObject.Aircraft.Communication.Network;
import aircraftsimulator.GameObject.Aircraft.MovingObjectInterface;
import aircraftsimulator.GameObject.Aircraft.Radar.Wave.ElectroMagneticWave;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.GameObject;
import aircraftsimulator.PaintDrawer;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.awt.*;

public class RadioEmitter extends Component implements RadarInterface{
    protected GameObject parent;
    protected final float power;
    protected final float frequency;
    protected final String code;

    protected final float angle;
    protected final float gain;
    protected final Color color;

    protected final double minimumDetectionIntensity;
    protected final double maxWaveRange;
    protected final double maxDetectionRange;

    private final double DEFAULT_RCS = 5 * 5 * Math.PI * 1000;

    public RadioEmitter(GameObject parent, Network network, float frequency, float power, float antennaDiameter, float detectionSNR) {
        this.frequency = frequency;
        this.power = power;
        this.parent = parent;
        code = parent.getTeam().getPW();

        angle = AngleFromArea(antennaDiameter, frequency);
        gain = calcGain();
        color = ElectroMagneticWave.GenerateFrequencyColor(frequency);
        System.out.println(calcEffectiveArea());
        minimumDetectionIntensity = Math.pow(10, detectionSNR / 10) * Environment.ENVIRONMENTAL_WAVE;

        maxWaveRange = getMaxWaveRange(Environment.ENVIRONMENTAL_WAVE);
        maxDetectionRange = getMaxDetectionRange(DEFAULT_RCS, minimumDetectionIntensity);
    }

    private float calcGain()
    {
        return 52525F / angle / angle;
    }

    private double calcEffectiveArea()
    {
        return gain * ElectroMagneticWave.LIGHT_SPEED * ElectroMagneticWave.LIGHT_SPEED / (4 * Math.PI * frequency * frequency);
    }

    protected void detect() {

    }

    @Override
    public void illuminate() {
        if (parent instanceof MovingObjectInterface m)
        {
//            environment.addLaser(new LaserInformation(new PositionInformationImp(parent, parent.getPosition()), frequency, power, m.getDirection(), angle, parent.getTeam().getTeamName(), PaintDrawer.radarColor), parent.getTeam());
            Environment.getInstance().addPulseWave(new ElectroMagneticWave(parent, parent.getPosition(), power * gain, frequency,  m.getDirection(), angle));
        }
//        else
//            environment.addLaser(new LaserInformation(new PositionInformationImp(parent, parent.getPosition()), frequency, power, new Vector3f(0, 0, 0), angle, parent.getTeam().getTeamName(), PaintDrawer.radarColor), parent.getTeam());
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
    public void update(float delta) {
        detect();
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(PaintDrawer.opacColor(color, 0.3F));
        Vector3f direction = ((MovingObjectInterface)parent).getDirection();
        Vector3f position = parent.getPosition();
        double centerAngle = GameMath.directionToAngle(new Vector2f(direction.x, direction.y)) % 360;
        g2d.fillArc((int)(position.x - maxWaveRange), (int)(position.y - maxWaveRange), (int)(maxWaveRange * 2), (int)(maxWaveRange * 2), (int)(centerAngle - angle / 2), (int)angle);
        g2d.fillArc((int)(position.x - maxDetectionRange), (int)(position.y - maxDetectionRange), (int)(maxDetectionRange * 2), (int)(maxDetectionRange * 2), (int)(centerAngle - angle / 2), (int)angle);
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
