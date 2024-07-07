package aircraftsimulator.GameObject.Aircraft.Radar.Radar;

import aircraftsimulator.Environment;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.LaserInformation;
import aircraftsimulator.GameObject.Aircraft.Communication.Information.PositionInformationImp;
import aircraftsimulator.GameObject.Aircraft.Communication.Network;
import aircraftsimulator.GameObject.Aircraft.Communication.NetworkComponent;
import aircraftsimulator.GameObject.Aircraft.Communication.SlowStartApplicationNetworkComponentImp;
import aircraftsimulator.GameObject.Aircraft.MovingObjectInterface;
import aircraftsimulator.GameObject.Aircraft.Radar.RadarData;
import aircraftsimulator.GameObject.Aircraft.SystemPort;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.GameObject;
import aircraftsimulator.GameObject.GameObjectInterface;
import aircraftsimulator.PaintDrawer;

import javax.vecmath.Vector3f;
import java.awt.*;

public class AngleRadar extends Component implements RadarInterface{
    private final float angle;
    protected GameObjectInterface parent;
    protected final float power;
    protected final float frequency;

    private final NetworkComponent networkComponent;

    public AngleRadar(MovingObjectInterface parent, Network network, float frequency, float range, float angle) {
        this.frequency = frequency;
        this.power = range;
        this.angle = angle;
        this.parent = parent;


        networkComponent = new SlowStartApplicationNetworkComponentImp(network){
            @Override
            public void handleConnectionEstablished(String sessionId, Integer port) {
                super.handleConnectionEstablished(sessionId, port);
                networkComponent.registerTimeout(SystemPort.SEARCH_RADAR, 1000, p -> {
                    illuminate();
                    networkComponent.updateTimeout(p, (long)(1000L / frequency));
                });
            }
        };
        networkComponent.openPort(SystemPort.SEARCH_RADAR);
    }

    @Override
    public void illuminate() {
        Environment environment = Environment.getInstance();
        if (parent instanceof MovingObjectInterface m)
            environment.addLaser(new LaserInformation(new PositionInformationImp(parent, parent.getPosition()), frequency, power, m.getDirection(), angle, parent.getTeam().getTeamName(), PaintDrawer.radarColor), parent.getTeam());
        else
            environment.addLaser(new LaserInformation(new PositionInformationImp(parent, parent.getPosition()), frequency, power, new Vector3f(0, 0, 0), angle, parent.getTeam().getTeamName(), PaintDrawer.radarColor), parent.getTeam());
        if(networkComponent.isConnected(SystemPort.SEARCH_RADAR))
            networkComponent.sendData(SystemPort.SEARCH_RADAR, new RadarData());
    }

    @Override
    public void update(float delta) {
//        illuminate();
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
}
