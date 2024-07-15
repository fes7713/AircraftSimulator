package aircraftsimulator.GameObject.Aircraft.CentralStrategy;

import aircraftsimulator.GameObject.Aircraft.Communication.Network;
import aircraftsimulator.GameObject.Aircraft.Communication.NetworkComponent;
import aircraftsimulator.GameObject.Aircraft.Communication.SlowStartApplicationNetworkComponentImp;
import aircraftsimulator.GameObject.Aircraft.SystemPort;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.GameObject;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SimpleStrategy extends Component {
    private GameObject parent;

    private Map<String, Vector3f> guideMap;
    private Map<String, Stack<Vector3f>> guideHistoryMap;
    private Map<String, Integer> guideLastFrameMap;

    private NetworkComponent networkComponent;
    private float iffTimeout;
    private float guideTimeout;

    public SimpleStrategy(GameObject parent, Network network)
    {
        this.parent = parent;
        networkComponent = new SlowStartApplicationNetworkComponentImp(network);
        networkComponent.openPort(SystemPort.STRATEGY);

        guideMap = new HashMap<>();
        guideHistoryMap = new HashMap<>();
        guideLastFrameMap = new HashMap<>();
    }

    @Override
    public void update(float delta) {

    }

    @Override
    public void draw(Graphics2D g2d) {

    }

    @Override
    public void setParent(GameObject parent) {

    }
}
