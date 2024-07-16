package aircraftsimulator.GameObject.Aircraft.CentralStrategy;

import aircraftsimulator.Game;
import aircraftsimulator.GameObject.Aircraft.Communication.Data.Data;
import aircraftsimulator.GameObject.Aircraft.Communication.Logger.Logger;
import aircraftsimulator.GameObject.Aircraft.Communication.Network;
import aircraftsimulator.GameObject.Aircraft.Communication.NetworkComponent;
import aircraftsimulator.GameObject.Aircraft.Communication.SlowStartApplicationNetworkComponentImp;
import aircraftsimulator.GameObject.Aircraft.Radar.RadarData;
import aircraftsimulator.GameObject.Aircraft.Radar.RadarFrequency;
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
    private final Map<String, TrackingState> trackingStateMap;


    private final NetworkComponent networkComponent;
    private final float trackingTimeout;
    private final float trackingSeparationDistance;

    private final float iffTimeout;
    private final float iffTravelTimoutMultipliplier;

    private static final float TRACKING_THRESHOLD = ElectroMagneticWave.LIGHT_SPEED * 5;
    private static final float TRACKING_TIMEOUT = 8;
    private static final Color TRACKING_POINT_COLOR = Color.WHITE;
    private static final int TRACKING_POINT_SIZE = 4;
    private static final int TRACKING_LOCK_SIZE = 10;

    private static final float IFF_TIMEOUT = 15000;
    private static final float IFF_TRAVEL_TIMOUT_MULTIPLIPLIER = 1.5F;

    private static final float COMMUNICATION_FREQUENCY = RadarFrequency.HF;


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
        trackingStateMap = new HashMap<>();

        iffTimeout = IFF_TIMEOUT;
        iffTravelTimoutMultipliplier = IFF_TRAVEL_TIMOUT_MULTIPLIPLIER;

        networkComponent.addDataReceiver(RadarData.class, (data, port) -> {
            processRadar(data.waves());
        });
        networkComponent.addDataReceiver(IFFSecretData.class, (data, port) -> {
            String pw = parent.getTeam().getPW();
            networkComponent.sendData(SystemPort.STRATEGY,
                    new DirectionalCommunicationData(new IFFResult(data.getSecret(pw)),
                            COMMUNICATION_FREQUENCY, data.getSource(pw), parent.getPosition()
                    )
            );
            Logger.Log(Logger.LogLevel.INFO, "IFF Result [%s]".formatted(data.getSecret(parent.getTeam().getPW())), networkComponent.getMac(), SystemPort.STRATEGY);
        });
        networkComponent.addDataReceiver(IFFResult.class, (data, port) -> {
            if(trackingMap.containsKey(data.secret()))
            {
                trackingStateMap.put(data.secret(), TrackingState.FRIENDLY);
                networkComponent.removeTimeout(data.secret());
                networkComponent.removeTimeout(data.secret() + "E");
            }
            Logger.Log(Logger.LogLevel.INFO, "IFF Result ACK [%s] [%s]".formatted(data.secret(), trackingMap.containsKey(data.secret()) ? "Friendly":"Enemy"), networkComponent.getMac(), SystemPort.STRATEGY);
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
                if(closestTrackingEntry.getValue() > trackingSeparationDistance * trackingSeparationDistance)
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
            if(!trackingStateMap.containsKey(trackingId))
            {
                trackingStateMap.put(trackingId, TrackingState.UNIDENTIFIED);
                sendIFF(trackingId, node.getPosition());
                networkComponent.registerTimeout(trackingId + "E", (long) (iffTimeout), s -> {
                    trackingStateMap.put(trackingId, TrackingState.ENEMY);
                    Logger.Log(Logger.LogLevel.INFO, "IFF No Respond Tracking ID [%s]".formatted(trackingId), networkComponent.getMac(), SystemPort.STRATEGY);
                    networkComponent.removeTimeout(trackingId);
                    networkComponent.removeTimeout(trackingId + "E");
                });
            }
            trackingMap.put(trackingId, node);
            nodes.add(node);
            trackingHistoryMap.put(trackingId, nodes);
        }
    }

    private void sendIFF(String trackingId, Vector3f trackingPosition)
    {
        networkComponent.sendData(SystemPort.STRATEGY,
                new DirectionalCommunicationData(
                        new IFFSecretData(trackingId, parent.getTeam().getPW(), parent.getPosition()),
                        COMMUNICATION_FREQUENCY,
                        trackingPosition,
                        parent.getPosition()
                )
        );
        Logger.Log(Logger.LogLevel.INFO, "IFF Request to Tracking ID [%s]".formatted(trackingId), networkComponent.getMac(), SystemPort.STRATEGY);
        Vector3f diff = new Vector3f(trackingPosition);
        diff.sub(parent.getPosition());
        float expectedTime = diff.length() / ElectroMagneticWave.LIGHT_SPEED;
        networkComponent.registerTimeout(trackingId, (long)(expectedTime * iffTravelTimoutMultipliplier * 1000), s -> {
            sendIFF(trackingId, trackingMap.get(trackingId).getPosition());
        });
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
            while(!trackingHistoryMap.get(id).isEmpty() && Game.getGameTime() - trackingHistoryMap.get(id).peek().getCreated() > trackingTimeout)
                trackingHistoryMap.get(id).poll();
            if(trackingHistoryMap.get(id).isEmpty())
            {
                trackingStateMap.remove(id);
                trackingMap.remove(id);
                trackingHistoryMap.remove(id);
                networkComponent.removeTimeout(id);
                networkComponent.removeTimeout(id + "E");
            }
        }
    }

    private void drawLock(Graphics2D g2d, Vector3f centerPosition, Color color, float opacity, int size)
    {
        g2d.setColor(PaintDrawer.opacColor(color, opacity));
        g2d.drawLine((int)(centerPosition.x - size), (int)(centerPosition.y - size),
                (int)(centerPosition.x - size), (int)(centerPosition.y - size / 2F));
        g2d.drawLine((int)(centerPosition.x - size), (int)(centerPosition.y - size),
                (int)(centerPosition.x - size / 2F), (int)(centerPosition.y - size));

        g2d.drawLine((int)(centerPosition.x + size), (int)(centerPosition.y - size),
                (int)(centerPosition.x + size), (int)(centerPosition.y - size / 2F));
        g2d.drawLine((int)(centerPosition.x + size), (int)(centerPosition.y - size),
                (int)(centerPosition.x + size / 2F), (int)(centerPosition.y - size));

        g2d.drawLine((int)(centerPosition.x - size), (int)(centerPosition.y + size),
                (int)(centerPosition.x - size), (int)(centerPosition.y + size / 2F));
        g2d.drawLine((int)(centerPosition.x - size), (int)(centerPosition.y + size),
                (int)(centerPosition.x - size / 2F), (int)(centerPosition.y + size));

        g2d.drawLine((int)(centerPosition.x + size), (int)(centerPosition.y + size),
                (int)(centerPosition.x + size), (int)(centerPosition.y + size / 2F));
        g2d.drawLine((int)(centerPosition.x + size), (int)(centerPosition.y + size),
                (int)(centerPosition.x + size / 2F), (int)(centerPosition.y + size));
    }

    @Override
    public void draw(Graphics2D g2d) {

        for(String trackingId: trackingHistoryMap.keySet())
        {
            Queue<TrackingNode> nodes = trackingHistoryMap.get(trackingId);
            TrackingNode prevNode = null;
            for(TrackingNode node: nodes)
            {
                g2d.setColor(PaintDrawer.opacColor(TRACKING_POINT_COLOR, 1 - (Game.getGameTime() - node.getCreated()) / trackingTimeout));
                if(prevNode != null)
                {
                    g2d.drawLine((int)(prevNode.getPosition().x), (int)(prevNode.getPosition().y),
                            (int)(node.getPosition().x), (int)(node.getPosition().y));
                }
                g2d.fillOval((int)(node.getPosition().x - TRACKING_POINT_SIZE /2), (int)(node.getPosition().y - TRACKING_POINT_SIZE /2), (int)TRACKING_POINT_SIZE, (int)TRACKING_POINT_SIZE);
                prevNode = node;
            }
        }
        for(String id: trackingMap.keySet())
        {
            drawLock(g2d, trackingMap.get(id).getPosition(), trackingStateMap.get(id).getColor(), 1 - (Game.getGameTime() - trackingMap.get(id).getCreated()) / trackingTimeout, TRACKING_LOCK_SIZE);
        }
    }

    @Override
    public void setParent(GameObject parent) {

    }
}
