package aircraftsimulator.GameObject.Aircraft.Radar.Radar;

import aircraftsimulator.Environment;
import aircraftsimulator.GameObject.Aircraft.Communication.Network;
import aircraftsimulator.GameObject.Aircraft.Communication.NetworkComponent;
import aircraftsimulator.GameObject.Aircraft.Communication.SlowStartApplicationNetworkComponentImp;
import aircraftsimulator.GameObject.Aircraft.MovingObjectInterface;
import aircraftsimulator.GameObject.Aircraft.Radar.RadarData;
import aircraftsimulator.GameObject.Aircraft.Radar.Wave.ElectroMagneticWave;
import aircraftsimulator.GameObject.Aircraft.SystemPort;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.GameObject;
import aircraftsimulator.GameObject.GameObjectInterface;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.List;

public class AngleRadar extends Component implements RadarInterface{
    protected GameObjectInterface parent;
    protected final float power;
    protected final float frequency;
    protected final long interval;
    private final float gain;
    protected final String code = "AA";

    private final float angle;

    private final NetworkComponent networkComponent;

    public AngleRadar(MovingObjectInterface parent, Network network, float frequency, long interval, float power, float antennaDiameter) {
        this.frequency = frequency;
        this.power = power;

        this.parent = parent;
        this.interval = interval;

        angle = AngleFromArea(antennaDiameter, frequency);
        gain = calcGain();

        networkComponent = new SlowStartApplicationNetworkComponentImp(network){
            @Override
            public void handleConnectionEstablished(String sessionId, Integer port) {
                super.handleConnectionEstablished(sessionId, port);
                networkComponent.registerTimeout(SystemPort.SEARCH_RADAR, 1000, p -> {
                    illuminate();
                    networkComponent.updateTimeout(p, interval);
                });
            }
        };
        networkComponent.openPort(SystemPort.SEARCH_RADAR);

        System.out.println("[%s] %s:%f, %s:%f".formatted("AngleRadar", "Angle", angle, "Gain", gain) );
    }

    private float calcGain()
    {
        return 52525F / angle / angle;
    }

    private void detect() {
        Environment environment = Environment.getInstance();
        List<Vector3f> posList = environment.detectWave((GameObject) parent, code);
        if(!posList.isEmpty())
        {
            networkComponent.sendData(SystemPort.SEARCH_RADAR, new RadarData(posList));
        }
    }

    @Override
    public void illuminate() {
        if (parent instanceof MovingObjectInterface m)
        {
//            environment.addLaser(new LaserInformation(new PositionInformationImp(parent, parent.getPosition()), frequency, power, m.getDirection(), angle, parent.getTeam().getTeamName(), PaintDrawer.radarColor), parent.getTeam());
            Environment.getInstance().addPulseWave(new ElectroMagneticWave(parent, parent.getPosition(), power * gain, frequency,  m.getDirection(), angle, code));
        }
//        else
//            environment.addLaser(new LaserInformation(new PositionInformationImp(parent, parent.getPosition()), frequency, power, new Vector3f(0, 0, 0), angle, parent.getTeam().getTeamName(), PaintDrawer.radarColor), parent.getTeam());
    }

    @Override
    public void update(float delta) {
        detect();
    }

    @Override
    public void draw(Graphics2D g2d) {

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
