package aircraftsimulator.GameObject.Aircraft.CentralStrategy;

import aircraftsimulator.Game;
import aircraftsimulator.GameObject.Aircraft.Communication.Data.Data;
import aircraftsimulator.GameObject.Aircraft.Communication.Network;
import aircraftsimulator.GameObject.Aircraft.Communication.NetworkComponent;
import aircraftsimulator.GameObject.Aircraft.Communication.SlowStartApplicationNetworkComponentImp;
import aircraftsimulator.GameObject.Aircraft.Radar.RadarData;
import aircraftsimulator.GameObject.Aircraft.Radar.Wave.ElectroMagneticWave;
import aircraftsimulator.GameObject.Aircraft.Radar.Wave.ElectroMagneticWaveData;
import aircraftsimulator.GameObject.Aircraft.SystemPort;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.GameObject;
import aircraftsimulator.PaintDrawer;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.stream.Collectors;

public class SimpleStrategy extends Component {
    private class TrackingNode implements Data
    {
        private final Vector3f position;
        private final float created;

        public TrackingNode(Vector3f position)
        {
            this.position = position;
            created = Game.getGameTime();
        }

        public Vector3f getPosition()
        {
            return position;
        }

        public float getCreated() {
            return created;
        }
    }

    private final GameObject parent;

    private final Map<String, TrackingNode> trackingMap;
    private final Map<String, Queue<TrackingNode>> trackingHistoryMap;


    private final NetworkComponent networkComponent;
    private float iffTimeout;
    private final float trackingTimeout;
    private final float trackingSeparationDistance;


    private static final float TRACKING_THRESHOLD = ElectroMagneticWave.LIGHT_SPEED * 5;
    private static final float TRACKING_TIMEOUT = 20;
    private static final Color TRACKING_POINT_COLOR = Color.WHITE;
    private static final int TRACKING_POINT_SIZE = 4;


    public SimpleStrategy(GameObject parent, Network network)
    {
        this(parent, network, TRACKING_TIMEOUT, TRACKING_THRESHOLD);
    }

    public SimpleStrategy(GameObject parent, Network network, float trackingTimeout, float trackingSeparationDistance)
    {
        this.parent = parent;
        this.trackingTimeout = trackingTimeout;
        this.trackingSeparationDistance = trackingSeparationDistance;

        networkComponent = new SlowStartApplicationNetworkComponentImp(network);
        networkComponent.openPort(SystemPort.STRATEGY);
        networkComponent.enabledPortTransfer(SystemPort.STRATEGY);

        trackingMap = new HashMap<>();
        trackingHistoryMap = new HashMap<>();

        networkComponent.addDataReceiver(RadarData.class, (data, port) -> {
            processRadar(data.waves());
        });
    }

    private void processRadar(List<ElectroMagneticWaveData> waves){
        Map<String, Vector3f> tempTracker = new HashMap<>();
        for(ElectroMagneticWaveData waveData: waves)
        {
            Map.Entry<String, Float> closestTrackingEntry = getClosestTrackingEntry(waveData.getPosition());
            if(closestTrackingEntry == null)
                tempTracker.put(UUID.randomUUID().toString(), waveData.getPosition());
            else
            {
                if(closestTrackingEntry.getValue() > TRACKING_THRESHOLD * TRACKING_THRESHOLD)
                {
                    tempTracker.put(UUID.randomUUID().toString(), waveData.getPosition());
                }else{
                    tempTracker.put(closestTrackingEntry.getKey(), waveData.getPosition());
                }
            }
        }
        for(String trackingId: tempTracker.keySet())
        {
            TrackingNode node = new TrackingNode(tempTracker.get(trackingId));
            Queue<TrackingNode> nodes = trackingHistoryMap.getOrDefault(trackingId, new ArrayDeque<>());
            trackingMap.put(trackingId, node);
            nodes.add(node);
            trackingHistoryMap.put(trackingId, nodes);
        }
    }

    private Map.Entry<String, Float> getClosestTrackingEntry(Vector3f position)
    {
        return trackingMap.entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> {
                    Vector3f diff = new Vector3f(trackingMap.get(e.getKey()).getPosition());
                    diff.sub(position);
                    return diff.lengthSquared();
                }))
                .entrySet()
                .stream()
                .min(Map.Entry.comparingByValue(Comparator.comparingDouble(Float::doubleValue)))
                .orElse(null);
    }

    @Override
    public void update(float delta) {
        for(String id: new HashSet<>(trackingHistoryMap.keySet()))
        {
            while(!trackingHistoryMap.get(id).isEmpty() && Game.getGameTime() - trackingHistoryMap.get(id).peek().getCreated() > TRACKING_TIMEOUT)
                trackingHistoryMap.get(id).poll();
            if(trackingHistoryMap.get(id).isEmpty())
            {
                trackingMap.remove(id);
                trackingHistoryMap.remove(id);
            }
        }
    }

    @Override
    public void draw(Graphics2D g2d) {

        for(String trackingId: trackingHistoryMap.keySet())
        {
            Queue<TrackingNode> nodes = trackingHistoryMap.get(trackingId);
            TrackingNode prevNode = null;
            for(TrackingNode node: nodes)
            {
                g2d.setColor(PaintDrawer.opacColor(TRACKING_POINT_COLOR, 1 - (Game.getGameTime() - node.getCreated()) / TRACKING_TIMEOUT));
                if(prevNode != null)
                {
                    g2d.drawLine((int)(prevNode.getPosition().x), (int)(prevNode.getPosition().y),
                            (int)(node.getPosition().x), (int)(node.getPosition().y));
                }
                g2d.fillOval((int)(node.getPosition().x - TRACKING_POINT_SIZE /2), (int)(node.getPosition().y - TRACKING_POINT_SIZE /2), (int)TRACKING_POINT_SIZE, (int)TRACKING_POINT_SIZE);
                prevNode = node;
            }
        }
    }

    @Override
    public void setParent(GameObject parent) {

    }
}
