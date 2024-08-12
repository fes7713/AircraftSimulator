package aircraftsimulator.GameObject.Aircraft.CentralStrategy;

import aircraftsimulator.Game;
import aircraftsimulator.GameMath;
import aircraftsimulator.GameObject.Aircraft.Communication.Data.Data;
import aircraftsimulator.GameObject.Aircraft.Communication.Logger.Logger;
import aircraftsimulator.GameObject.Aircraft.Communication.Network;
import aircraftsimulator.GameObject.Aircraft.Communication.NetworkComponent;
import aircraftsimulator.GameObject.Aircraft.Communication.SlowStartApplicationNetworkComponentImp;
import aircraftsimulator.GameObject.Aircraft.Radar.Radar.TrackingRequest;
import aircraftsimulator.GameObject.Aircraft.Radar.RadarData;
import aircraftsimulator.GameObject.Aircraft.Radar.RadarFrequency;
import aircraftsimulator.GameObject.Aircraft.Radar.Wave.ElectroMagneticWave;
import aircraftsimulator.GameObject.Aircraft.Radar.Wave.ElectroMagneticWaveData;
import aircraftsimulator.GameObject.Aircraft.SystemPort;
import aircraftsimulator.GameObject.Component.Component;
import aircraftsimulator.GameObject.GameObject;
import aircraftsimulator.PaintDrawer;
import aircraftsimulator.Trendline.PolyTrendLine;

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

        public TrackingNode(Vector3f position, float created)
        {
            this.position = position;
            this.created = created;
        }

        public Vector3f getPosition()
        {
            return position;
        }

        public float getCreated() {
            return created;
        }

        @Override
        public String toString() {
            return String.format("%.4f,%.4f", position.x, position.y);
        }
    }

    private record TrackingInfo(float recordedTime, Vector3f acceleration, Vector3f velocity, Vector3f position, float angularSpeed) { }

    private final GameObject parent;

    private final Map<String, LinkedList<TrackingNode>> trackingHistoryMap;
    private final Map<String, TrackingState> trackingStateMap;
    private final Map<String, TrackingInfo> trackingInfoMap;

    private final Map<String, TrackingNode> futurePositionTrackingMap;
    private final Map<String, LinkedList<TrackingNode>> futurePositionTrackingHistoryMap;

    private final NetworkComponent networkComponent;
    private final float trackingTimeout;
    private final float trackingSeparationDistance;

    private final float iffTimeout;
    private final float iffTravelTimoutMultipliplier;

    private static final float TRACKING_THRESHOLD = ElectroMagneticWave.LIGHT_SPEED * 8;
    private static final float TRACKING_TIMEOUT = 50;
    private static final Color TRACKING_POINT_COLOR = Color.WHITE;
    private static final int TRACKING_POINT_SIZE = 4;
    private static final int TRACKING_LOCK_SIZE = 10;

    private static final float TRACKING_INTERVAL = 1.5F;

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
        trackingInfoMap = new HashMap<>();

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
            if(!trackingHistoryMap.containsKey(trackingId))
                trackingHistoryMap.put(trackingId, new LinkedList<>());
            LinkedList<TrackingNode> nodes = trackingHistoryMap.get(trackingId);

            TrackingNode node;
            if(nodes.size() == 0)
                node = new TrackingNode(tempTracker.get(trackingId), waves.get(0).getCreated());
            else if(nodes.size() == 1)
                node = new TrackingNode(tempTracker.get(trackingId), waves.get(0).getCreated());
            else
            {
                Vector3f velocity;
                Vector3f acceleration;
//                LinkedList<TrackingNode> samplingNodesNew = new LinkedList<>();
//                LinkedList<TrackingNode> samplingNodesOld = new LinkedList<>();
//
//                Vector3f velocityNew = GameMath.getVelocityVector(tempTracker.get(trackingId), nodes.getLast().getPosition(), waves.get(0).getCreated() - nodes.getLast().getCreated());
//                Vector3f accelerationNew = GameMath.getAccelerationVector(velocityNew, nodes.getLast().getVelocity(), waves.get(0).getCreated() - nodes.getLast().getCreated());
//
//                samplingNodesNew.add(new TrackingNode(tempTracker.get(trackingId),
//                        velocityNew, accelerationNew, accelerationNew.length() / velocityNew.length(), waves.get(0).getCreated()));
//
//                for(int i = nodes.size() - 1; i >= 1; i--)
//                {
//                    if(waves.get(0).getCreated() - nodes.get(i).getCreated() > TRACKING_INTERVAL)
//                        break;
//                    else if(waves.get(0).getCreated() - nodes.get(i).getCreated() > TRACKING_INTERVAL / 2)
//                        samplingNodesOld.add(nodes.get(i));
//                    else
//                        samplingNodesNew.add(nodes.get(i));
//                }
////                float angularSpeed;
//                // Sampling
//                if(!samplingNodesNew.isEmpty() && !samplingNodesOld.isEmpty())
//                {
//                    if(samplingNodesNew.size() < 3 || samplingNodesOld.size() < 3)
//                    {
//                        velocity = GameMath.getVelocityVector(tempTracker.get(trackingId), nodes.getLast().getPosition(), waves.get(0).getCreated() - nodes.getLast().getCreated());
//                        acceleration = GameMath.getAccelerationVector(velocity, samplingNodesOld.getLast().getVelocity(), waves.get(0).getCreated() - nodes.getLast().getCreated());
//                    }
//                    else{
//                        velocity = GameMath.vector3fAveraging(samplingNodesNew.stream().map(trackingNode -> trackingNode.velocity).toList());
//                        acceleration = GameMath.getAccelerationVector(
//                                velocity,
//                                GameMath.vector3fAveraging(samplingNodesOld.stream().map(trackingNode -> trackingNode.velocity).toList()),
//                                waves.get(0).getCreated() - samplingNodesOld.getLast().getCreated());
//                    }
//                }
//                // Direct calc
//                else{

//                }
                node = new TrackingNode(tempTracker.get(trackingId), waves.get(0).getCreated());
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
                    networkComponent.registerTimeout(trackingId + "T", 1000,  s1 -> {
                        if(trackingInfoMap.containsKey(trackingId))
                        {
                            Vector3f position = getFuturePoints(trackingId, Arrays.asList(Game.getGameTime())).get(0);
                            futurePositionTrackingMap.put(trackingId, new TrackingNode(position, Game.getGameTime()));
                            networkComponent.sendData(SystemPort.STRATEGY, new TrackingRequest(s1, position));
                        }
                        networkComponent.updateTimeout(s1, 1000);
                    });
                });
            }
            nodes.add(node);
            trackingUpdate(trackingId);

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
//        TrackingNode currentNode = generateTrackPoint(futurePositionTrackingHistoryMap.get(trackingId), Game.getGameTime(), futurePositionTrackingHistoryMap.get(trackingId).getLast().getAngularSpeed());

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

//    private TrackingNode generateTrackPoint(List<TrackingNode> nodes, float time, float angularSpeed)
//    {
//        int index = -1;
//        for(int i = nodes.size() - 1; i >= 0; i--)
//        {
//            if(time - nodes.get(i).getCreated() > 0)
//            {
//                index = i;
//                break;
//            }
//        }
//
//        if(index == -1)
//        {
//            System.err.println("Cannot predict past");
//            return null;
//        }
//
//        Vector3f targetDirection = new Vector3f();
//        if(index > 10)
//            targetDirection.set(nodes.get(index - 5).getVelocity());
//        else
//            targetDirection.set(nodes.get(0).getVelocity());
//        targetDirection.negate();
//
//        Vector3f position = new Vector3f(nodes.get(index).getPosition());
//        Vector3f velocity = new Vector3f(nodes.get(index).getVelocity());
//
//        float timeDiff = time  - nodes.get(index).getCreated();
//        if(angularSpeed >= 0.000001F)
//        {
//            float radian = angularSpeed * timeDiff;
//            Vector3f newVelocity = GameMath.rotatedDirection(radian, velocity, targetDirection);
//            newVelocity.normalize();
//            newVelocity.scale(velocity.length());
//            velocity.set(newVelocity);
//        }
//        Vector3f positionDiff = new Vector3f(velocity);
//        positionDiff.scale(timeDiff);
//        position.add(positionDiff);
//        return new TrackingNode(new Vector3f(position), new Vector3f(velocity), new Vector3f(0, 0, 0), angularSpeed, time);
//    }

    private void trackingUpdate(String id)
    {
        if(trackingHistoryMap.get(id).size() <= 2)
            return;

        LinkedList<TrackingNode> nodes = new LinkedList<>(trackingHistoryMap.get(id));

        LinkedList<TrackingNode> samplingNodes = new LinkedList<>();
        for(int i = nodes.size() - 1; i >= 0; i--)
        {
            if(nodes.getLast().getCreated() - nodes.get(i).getCreated() < TRACKING_INTERVAL * 3)
                samplingNodes.add(nodes.get(i));
        }


        float angularSpeed;
        synchronized (futurePositionTrackingHistoryMap)
        {
            futurePositionTrackingHistoryMap.get(id).clear();
        }
        if(samplingNodes.size() < 2) {
            return;
        }
        if(samplingNodes.size() < 4){
            angularSpeed = 0;
            Vector3f acceleration = new Vector3f(0, 0, 0);
            Vector3f velocity = new Vector3f(samplingNodes.getLast().getPosition());
            velocity.sub(samplingNodes.get(samplingNodes.size() - 2).getPosition());
            velocity.scale(1 / (samplingNodes.getLast().getCreated() - samplingNodes.get(samplingNodes.size() - 2).getCreated()));
            trackingInfoMap.put(id, new TrackingInfo(samplingNodes.getLast().getCreated(), acceleration, velocity, samplingNodes.getLast().getPosition(), 0));
        }
        else
        {
            System.out.println("Tracking size " + samplingNodes.size());
            double[] t = new double[samplingNodes.size()];
            double[] x = new double[samplingNodes.size()];
            double[] y = new double[samplingNodes.size()];
            double[] z = new double[samplingNodes.size()];

            for (int i = 0; i < samplingNodes.size(); i++)
            {
                t[i] = trackingHistoryMap.get(id).get(trackingHistoryMap.get(id).size() - i - 1).getCreated();
                x[i] = trackingHistoryMap.get(id).get(trackingHistoryMap.get(id).size() - i - 1).getPosition().x;
                y[i] = trackingHistoryMap.get(id).get(trackingHistoryMap.get(id).size() - i - 1).getPosition().y;
                z[i] = trackingHistoryMap.get(id).get(trackingHistoryMap.get(id).size() - i - 1).getPosition().z;
            }
            PolyTrendLine trendLineX = new PolyTrendLine(2);
            PolyTrendLine trendLineY = new PolyTrendLine(2);
            PolyTrendLine trendLineZ = new PolyTrendLine(2);
            trendLineX.setValues(x, t);
            trendLineY.setValues(y, t);
            trendLineZ.setValues(z, t);

            double[] parametersX = trendLineX.getParameters().getColumn(0);
            double[] parametersY = trendLineY.getParameters().getColumn(0);
            double[] parametersZ = trendLineZ.getParameters().getColumn(0);

            Vector3f currentPosition = new Vector3f(
                (float)(parametersX[0] + parametersX[1] * Game.getGameTime() + parametersX[2] * Game.getGameTime() * Game.getGameTime()),
                (float)(parametersY[0] + parametersY[1] * Game.getGameTime() + parametersY[2] * Game.getGameTime() * Game.getGameTime()),
                (float)(parametersZ[0] + parametersZ[1] * Game.getGameTime() + parametersZ[2] * Game.getGameTime() * Game.getGameTime())
            );
            Vector3f currentVelocity = new Vector3f((float)(parametersX[1] + 2 * parametersX[2] * Game.getGameTime()), (float)(parametersY[1] + 2 * parametersY[2] * Game.getGameTime()), (float)(parametersZ[1] + 2 * parametersZ[2] * Game.getGameTime()));
            Vector3f acceleration = new Vector3f((float)(2 * parametersX[2]), (float)(2 * parametersY[2]), (float)(2 * parametersZ[2]));

            Vector3f accelerationCent = GameMath.getPerpendicularComponent(acceleration, currentVelocity);
            angularSpeed = accelerationCent.length() / currentVelocity.length();
            trackingInfoMap.put(id, new TrackingInfo(Game.getGameTime(), acceleration, currentVelocity, currentPosition, angularSpeed));
        }

        float timePassed = ((int)(Game.getGameTime() / TRACKING_INTERVAL)) * TRACKING_INTERVAL;
        float shift = - (Game.getGameTime() - timePassed);

        List<Float> times = new ArrayList<>();
        for(int i = 0; i < 20; i++)
            times.add(shift + i * TRACKING_INTERVAL + Game.getGameTime());

        List<Vector3f> positions = getFuturePoints(id, times);

        for (int i = 0; i < positions.size(); i++) {
            TrackingNode node = new TrackingNode(positions.get(i), times.get(i));
            futurePositionTrackingHistoryMap.get(id).add(node);
        }

//        TrackingNode latestNode = trackingHistoryMap.get(id).getLast();
//
//        float timeDiff = TRACKING_INTERVAL;
//        float timePassed = ((int)(latestNode.getCreated() / timeDiff)) * timeDiff;
//
//        List<TrackingNode> trackingNodes = new ArrayList<>(nodes);
//        trackingNodes.addAll(futurePositionTrackingHistoryMap.get(id));
//        trackingNodes.sort((o1, o2) -> (int) Math.floor(o1.getCreated() - o2.getCreated()));
//        for(int i = 0; i < 40; i++)
//        {
//            TrackingNode node = generateTrackPoint(trackingNodes, timePassed, angularSpeed);
//
//            if(node != null)
//                futurePositionTrackingHistoryMap.get(id).add(node);
//            else
//                break;
//
//            timePassed += timeDiff;
//            timeDiff = TRACKING_INTERVAL;
//        }
    }

    public List<Vector3f> getFuturePoints(String id, List<Float> times)
    {
        TrackingInfo info = trackingInfoMap.get(id);
        Vector3f accelerationCent = GameMath.getPerpendicularComponent(info.acceleration(), info.velocity());

        List<Vector3f> positions = new ArrayList<>();
        if(info.angularSpeed() > 0.001F) {
            positions = GameMath.futurePos(info.position(), accelerationCent, info.velocity(), times, info.recordedTime());
        }else {
            for(int i = 0; i < times.size(); i++)
            {
                Vector3f position = new Vector3f(info.velocity());
                position.scale(times.get(i) - info.recordedTime());
                position.add(info.position());
                positions.add(position);
            }
        }
        return positions;
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
                trackingInfoMap.remove(id);
                trackingHistoryMap.remove(id);
                futurePositionTrackingMap.remove(id);
                futurePositionTrackingHistoryMap.remove(id);
                networkComponent.removeTimeout(id);
                networkComponent.removeTimeout(id + "E");
                networkComponent.removeTimeout(id + "T");
            }
        }

        for(String id: trackingHistoryMap.keySet())
        {
            if(trackingHistoryMap.get(id).size() <= 2)
                break;
//            List<TrackingNode> trackingNodes = new ArrayList<>(trackingHistoryMap.get(id));
//            trackingNodes.addAll(futurePositionTrackingHistoryMap.get(id));
//            trackingNodes.sort((o1, o2) -> (int) Math.floor(o1.getCreated() - o2.getCreated()));
//            trackingNodes.remove(0);
//            float angularSpeed = GameMath.additiveAverage(trackingHistoryMap.get(id).stream().map(TrackingNode::getAngularSpeed).collect(Collectors.toList()));
//
//            futurePositionTrackingMap.put(id, generateTrackPoint(trackingNodes, Game.getGameTime(), angularSpeed));
            if(trackingInfoMap.containsKey(id))
            {
                Vector3f position = getFuturePoints(id, Arrays.asList(Game.getGameTime())).get(0);
                futurePositionTrackingMap.put(id, new TrackingNode(position, Game.getGameTime()));
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

    private void drawTrackingNode(Graphics2D g2d, String id, TrackingNode prevNode, TrackingNode node)
    {
        if(prevNode != null)
        {
            g2d.drawLine((int)(prevNode.getPosition().x), (int)(prevNode.getPosition().y),
                    (int)(node.getPosition().x), (int)(node.getPosition().y));
        }
        g2d.fillOval((int)(node.getPosition().x - TRACKING_POINT_SIZE /2), (int)(node.getPosition().y - TRACKING_POINT_SIZE /2), (int)TRACKING_POINT_SIZE, (int)TRACKING_POINT_SIZE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 4));
        g2d.drawString(String.format("[%2.2f]%2.8f", node.getCreated(), trackingInfoMap.containsKey(id) ? trackingInfoMap.get(id).angularSpeed() : 0), (int)node.getPosition().x, (int)(node.getPosition().y));
    }

    @Override
    public void draw(Graphics2D g2d) {

        synchronized (trackingHistoryMap)
        {
            for(String trackingId: trackingHistoryMap.keySet())
            {
                Queue<TrackingNode> nodes = trackingHistoryMap.get(trackingId);
                TrackingNode prevNode = null;
                for(TrackingNode node: new ArrayList<>(nodes))
                {
                    g2d.setColor(PaintDrawer.opacColor(TRACKING_POINT_COLOR, 1 - (Game.getGameTime() - node.getCreated()) / trackingTimeout));
                    drawTrackingNode(g2d, trackingId, prevNode, node);
                    prevNode = node;
                }
            }
        }

        for(String id: trackingHistoryMap.keySet())
        {
            drawLock(g2d, trackingHistoryMap.get(id).getLast().getPosition(), trackingStateMap.get(id).getColor(), 1 - (Game.getGameTime() - trackingHistoryMap.get(id).getLast().getCreated()) / trackingTimeout, TRACKING_LOCK_SIZE);
        }

        float dash1[] = {TRACKING_INTERVAL * 2, TRACKING_INTERVAL};
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
                    drawTrackingNode(g2d, trackingId, prevNode, nodes.get(i));
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
