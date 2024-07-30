package aircraftsimulator.GameObject.Aircraft.CentralStrategy;

import aircraftsimulator.Game;
import aircraftsimulator.GameMath;
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
        private final Vector3f velocity;
        private final Vector3f acceleration;
        private final float angularSpeed;
        private final float created;

        public TrackingNode(Vector3f position, Vector3f velocity, Vector3f acceleration, float angularSpeed, float created)
        {
            this.position = position;
            this.velocity = velocity;
            this.acceleration = acceleration;
            this.angularSpeed = angularSpeed;
            this.created = created;
        }

        public Vector3f getPosition()
        {
            return position;
        }

        public Vector3f getVelocity(){
            return velocity;
        }

        public Vector3f getAcceleration()
        {
            return acceleration;
        }

        public float getAngularSpeed()
        {
            return angularSpeed;
        }

        public float getCreated() {
            return created;
        }
    }

    private final GameObject parent;

    private final Map<String, LinkedList<TrackingNode>> trackingHistoryMap;
    private final Map<String, TrackingState> trackingStateMap;

    private final Map<String, TrackingNode> futurePositionTrackingMap;
    private final Map<String, LinkedList<TrackingNode>> futurePositionTrackingHistoryMap;

    private final NetworkComponent networkComponent;
    private final float trackingTimeout;
    private final float trackingSeparationDistance;

    private final float iffTimeout;
    private final float iffTravelTimoutMultipliplier;

    private static final float TRACKING_THRESHOLD = ElectroMagneticWave.LIGHT_SPEED * 8;
    private static final float TRACKING_TIMEOUT = 20;
    private static final Color TRACKING_POINT_COLOR = Color.WHITE;
    private static final int TRACKING_POINT_SIZE = 4;
    private static final int TRACKING_LOCK_SIZE = 10;

    private static final float FUTURE_PREDICTION_INTERVAL = 2F;

    private static final float IFF_TIMEOUT = 15000;
    private static final float IFF_TRAVEL_TIMEOUT_MULTIPLIER = 1.2F;

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

        trackingHistoryMap = new HashMap<>();
        trackingStateMap = new HashMap<>();

        futurePositionTrackingMap = new HashMap<>();
        futurePositionTrackingHistoryMap = new HashMap<>();

        iffTimeout = IFF_TIMEOUT;
        iffTravelTimoutMultipliplier = IFF_TRAVEL_TIMEOUT_MULTIPLIER;

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
            if(trackingHistoryMap.containsKey(data.secret()))
            {
                trackingStateMap.put(data.secret(), TrackingState.FRIENDLY);
                networkComponent.removeTimeout(data.secret());
                networkComponent.removeTimeout(data.secret() + "E");
            }
            Logger.Log(Logger.LogLevel.INFO, "IFF Result ACK [%s] [%s]".formatted(data.secret(), trackingHistoryMap.containsKey(data.secret()) ? "Friendly":"Enemy"), networkComponent.getMac(), SystemPort.STRATEGY);
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
            LinkedList<TrackingNode> nodes = trackingHistoryMap.getOrDefault(trackingId, new LinkedList<>());

            TrackingNode node;
            if(nodes.size() == 0)
                node = new TrackingNode(tempTracker.get(trackingId), null, null, 0, waves.get(0).getCreated());
            else if(nodes.size() == 1)
                node = new TrackingNode(tempTracker.get(trackingId),
                        GameMath.getVelocityVector(tempTracker.get(trackingId), nodes.get(0).getPosition(), waves.get(0).getCreated() - nodes.get(0).getCreated()),
                        null, 0, waves.get(0).getCreated());
            else
            {
                Vector3f velocity = GameMath.getVelocityVector(tempTracker.get(trackingId), nodes.getLast().getPosition(), waves.get(0).getCreated() - nodes.getLast().getCreated());
                        node = new TrackingNode(tempTracker.get(trackingId),
                        velocity,
                        GameMath.getAccelerationVector(velocity, nodes.getLast().getVelocity(), waves.get(0).getCreated() - nodes.getLast().getCreated())
                        , 0, waves.get(0).getCreated());
            }

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
            nodes.add(node);
            trackingHistoryMap.put(trackingId, nodes);
            if(!futurePositionTrackingHistoryMap.containsKey(trackingId))
                futurePositionTrackingHistoryMap.put(trackingId, new LinkedList<>());
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
            sendIFF(trackingId, trackingHistoryMap.get(trackingId).getLast().getPosition());
        });
    }

    private Map.Entry<String, Float> getClosestTrackingEntry(Vector3f position)
    {
        return trackingHistoryMap.entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> {
                    Vector3f diff = new Vector3f(trackingHistoryMap.get(e.getKey()).getLast().getPosition());
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
            synchronized (trackingHistoryMap)
            {
                while(!trackingHistoryMap.get(id).isEmpty() && Game.getGameTime() - trackingHistoryMap.get(id).peek().getCreated() > trackingTimeout)
                    trackingHistoryMap.get(id).poll();
            }
            if(trackingHistoryMap.get(id).isEmpty())
            {
                trackingStateMap.remove(id);
                trackingHistoryMap.remove(id);
                futurePositionTrackingMap.remove(id);
                futurePositionTrackingHistoryMap.remove(id);
                networkComponent.removeTimeout(id);
                networkComponent.removeTimeout(id + "E");
            }
        }

        for(String id: trackingHistoryMap.keySet())
        {
            if(trackingHistoryMap.get(id).size() == 1)
                continue;

            Vector3f position = new Vector3f(trackingHistoryMap.get(id).getLast().getPosition());
            Vector3f velocity = new Vector3f(trackingHistoryMap.get(id).getLast().getVelocity());
            Vector3f acceleration = new Vector3f(0, 0, 0);
            Vector3f targetDirection = new Vector3f();
            float angularSpeed = 0;


            if(trackingHistoryMap.get(id).size() == 2)
            {

            }
            else{
                List<TrackingNode> nodes = new ArrayList<>(trackingHistoryMap.get(id));
                nodes.remove(0);nodes.remove(0);

                acceleration = new Vector3f(nodes.get(nodes.size() - 1).getVelocity());
                angularSpeed = GameMath.additiveAverage(nodes.stream().map(trackingNode -> trackingNode.getAcceleration().length()).collect(Collectors.toList()))
                        / velocity.length();
                targetDirection.set(nodes.get(0).getVelocity());
                targetDirection.negate();
            }
            TrackingNode latestNode = trackingHistoryMap.get(id).getLast();


            float timeDiff = FUTURE_PREDICTION_INTERVAL;
            float timePassed = ((int)(latestNode.getCreated() / timeDiff)) * timeDiff;
            timeDiff = timePassed - latestNode.getCreated();

            synchronized (futurePositionTrackingHistoryMap)
            {
                futurePositionTrackingHistoryMap.get(id).clear();
            }

            System.out.println(angularSpeed);
            for(int i = 0; i < 20; i++)
            {
                if(angularSpeed >= 0.00000001F)
                {
                    float radian = angularSpeed * timeDiff;
                    Vector3f newVelocity = GameMath.rotatedDirection(radian, velocity, targetDirection);
                    newVelocity.normalize();
                    newVelocity.scale(velocity.length());
                    velocity.set(newVelocity);
                }
                Vector3f positionDiff = new Vector3f(velocity);
                positionDiff.scale(timeDiff);
                position.add(positionDiff);
                TrackingNode node = new TrackingNode(new Vector3f(position), new Vector3f(velocity), new Vector3f(acceleration), angularSpeed, timePassed);
                futurePositionTrackingHistoryMap.get(id).add(node);

                timeDiff = FUTURE_PREDICTION_INTERVAL;


                if(timePassed < Game.getGameTime() && timePassed + timeDiff > Game.getGameTime())
                {
                    float timeDiffCurrent =  Game.getGameTime() - timePassed;
                    float radian = angularSpeed * timeDiffCurrent;
                    Vector3f newVelocity = GameMath.rotatedDirection(radian, velocity, targetDirection);
                    newVelocity.normalize();
                    newVelocity.scale(velocity.length());
                    Vector3f positionCurrent = new Vector3f(newVelocity);
                    positionCurrent.scale(timeDiffCurrent);
                    positionCurrent.add(position);
                    futurePositionTrackingMap.put(id, new TrackingNode(positionCurrent, newVelocity, new Vector3f(acceleration), angularSpeed, Game.getGameTime()));
                }

                timePassed += timeDiff;
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

    private void drawTrackingNode(Graphics2D g2d, TrackingNode prevNode, TrackingNode node)
    {
        if(prevNode != null)
        {
            g2d.drawLine((int)(prevNode.getPosition().x), (int)(prevNode.getPosition().y),
                    (int)(node.getPosition().x), (int)(node.getPosition().y));
        }
        g2d.fillOval((int)(node.getPosition().x - TRACKING_POINT_SIZE /2), (int)(node.getPosition().y - TRACKING_POINT_SIZE /2), (int)TRACKING_POINT_SIZE, (int)TRACKING_POINT_SIZE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 4));
        g2d.drawString(String.format("[%2.2f]%2.8f", node.getCreated(), node.getAngularSpeed()), (int)node.getPosition().x, (int)(node.getPosition().y));
    }

    @Override
    public void draw(Graphics2D g2d) {

        synchronized (trackingHistoryMap)
        {
            for(String trackingId: trackingHistoryMap.keySet())
            {
                Queue<TrackingNode> nodes = trackingHistoryMap.get(trackingId);
                TrackingNode prevNode = null;
                for(TrackingNode node: nodes)
                {
                    g2d.setColor(PaintDrawer.opacColor(TRACKING_POINT_COLOR, 1 - (Game.getGameTime() - node.getCreated()) / trackingTimeout));
                    drawTrackingNode(g2d, prevNode, node);
                    prevNode = node;
                }
            }
        }

        for(String id: trackingHistoryMap.keySet())
        {
            drawLock(g2d, trackingHistoryMap.get(id).getLast().getPosition(), trackingStateMap.get(id).getColor(), 1 - (Game.getGameTime() - trackingHistoryMap.get(id).getLast().getCreated()) / trackingTimeout, TRACKING_LOCK_SIZE);
        }

        float dash1[] = {FUTURE_PREDICTION_INTERVAL * 2, FUTURE_PREDICTION_INTERVAL};
        BasicStroke dsahStroke1 = new BasicStroke(1.0f,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER,
                3.0f,
                dash1,
                0.0f);
        g2d.setStroke(dsahStroke1);

        for(String trackingId: futurePositionTrackingHistoryMap.keySet())
        {
            LinkedList<TrackingNode> nodes = futurePositionTrackingHistoryMap.get(trackingId);

            synchronized (futurePositionTrackingHistoryMap)
            {
                TrackingNode prevNode = null;
                for(int i = 0; i < nodes.size(); i++)
                {
                    drawTrackingNode(g2d, prevNode, nodes.get(i));
                    prevNode = nodes.get(i);
                }
            }

        }

        for(TrackingNode node: futurePositionTrackingMap.values())
        {
            g2d.drawOval((int)(node.getPosition().x - TRACKING_POINT_SIZE /2), (int)(node.getPosition().y - TRACKING_POINT_SIZE /2), (int)TRACKING_POINT_SIZE, (int)TRACKING_POINT_SIZE);
        }

        g2d.setStroke(new BasicStroke());
    }

    @Override
    public void setParent(GameObject parent) {

    }
}
