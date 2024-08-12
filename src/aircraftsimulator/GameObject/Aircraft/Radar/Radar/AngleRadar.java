package aircraftsimulator.GameObject.Aircraft.Radar.Radar;

import aircraftsimulator.Environment;
import aircraftsimulator.GameMath;
import aircraftsimulator.GameObject.Aircraft.Communication.Network;
import aircraftsimulator.GameObject.Aircraft.Communication.NetworkComponent;
import aircraftsimulator.GameObject.Aircraft.Communication.SlowStartApplicationNetworkComponentImp;
import aircraftsimulator.GameObject.Aircraft.Radar.RadarData;
import aircraftsimulator.GameObject.Aircraft.Radar.Wave.ElectroMagneticWave;
import aircraftsimulator.GameObject.Aircraft.Radar.Wave.ElectroMagneticWaveData;
import aircraftsimulator.GameObject.Aircraft.SystemPort;
import aircraftsimulator.GameObject.GameObject;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.util.*;

public class AngleRadar extends RadioEmitter implements RadarInterface{
    protected final NetworkComponent networkComponent;

    protected final long interval;

    protected RadarMode mode;

    protected final Map<String, Vector3f> trackingMap;
    protected final Queue<String> trackingQueue;

    public AngleRadar(GameObject parent, Network network, float frequency, long interval, float power, float radarMaxAngle, float antennaDiameter, float detectionSNR) {
        super(parent, network, frequency, power, radarMaxAngle, antennaDiameter, detectionSNR);

        this.interval = interval;
        trackingMap = new HashMap<>();
        trackingQueue = new ArrayDeque<>();

        networkComponent = new SlowStartApplicationNetworkComponentImp(network){
            @Override
            public void handleConnectionEstablished(String sessionId, Integer port) {
                super.handleConnectionEstablished(sessionId, port);
                networkComponent.registerTimeout(SystemPort.SEARCH_RADAR, 1000, p -> {
                    if(!active)
                        return;
                    switch (mode)
                    {
                        case SEARCHING -> searchTarget();
                        case TRACKING -> trackTarget();
                        case null -> setActive(false);
                    }
                    illuminate();
                    networkComponent.updateTimeout(p, interval);
                });
            }
        };
        networkComponent.openPort(SystemPort.SEARCH_RADAR);
        networkComponent.enabledPortTransfer(SystemPort.SEARCH_RADAR);

        networkComponent.addDataReceiver(SearchingRequest.class, (data, port) -> {
            mode = RadarMode.SEARCHING;
            setActive(true);
            networkComponent.sendData(SystemPort.SEARCH_RADAR, new RadarRequestAck(data.uuid()));
        });

        networkComponent.addDataReceiver(TrackingRequest.class, (data, port) -> {
            mode = RadarMode.TRACKING;
            setActive(true);
            trackingMap.put(data.uuid(), data.position());
        });



        System.out.println("[%s] %s:%f, %s:%f".formatted("AngleRadar", "Angle", beamAngle, "Gain", gain) );
    }

    protected void detect() {
        Environment environment = Environment.getInstance();
        List<ElectroMagneticWave> posList = environment.detectWave(parent);
        List<ElectroMagneticWaveData> detectedPosList = new ArrayList<>();
        for(ElectroMagneticWave wave: posList)
            if(wave.getIntensity() > minimumDetectionIntensity && wave.getFrequency() == frequency)
            {
                detectedPosList.add(new ElectroMagneticWaveData(wave, parent.getPosition()));
            }
        if(!detectedPosList.isEmpty())
        {
            networkComponent.sendData(SystemPort.SEARCH_RADAR, new RadarData(detectedPosList));
        }
    }

    private void searchTarget()
    {
        if(!setHorizontalAngle(radarHorizontalAngle - beamAngle))
        {
            setHorizontalAngle(360);
            System.out.println(radarVerticalAngle);
            if(!setVerticalAngle(radarVerticalAngle - beamAngle))
                setVerticalAngle(360);
        }
    }

    private void trackTarget()
    {
        if(trackingMap.isEmpty())
        {
            mode = RadarMode.SEARCHING;
            return;
        }
        if(trackingQueue.isEmpty())
            trackingQueue.addAll(trackingMap.keySet());

        String trackingId = trackingQueue.poll();

        Vector3f diff = new Vector3f(trackingMap.get(trackingId));
        diff.sub(parent.getPosition());

        Vector2f diffHorizontal = new Vector2f(diff.x, diff.y);
        Vector2f direction2D = new Vector2f(direction.x, direction.y);
        double horizontalRad = diffHorizontal.angle(direction2D) * Math.signum(GameMath.crossProduct2D(diffHorizontal, direction2D)) * -1;
        float verticalRad = (float) (
                Math.atan(Math.sqrt(diff.x * diff.x + diff.y * diff.y) / diff.z) -
                        Math.atan(Math.sqrt(direction.x * direction.x + direction.y * direction.y) / direction.z)
        );

        boolean result = setHorizontalAngle((float)Math.toDegrees(horizontalRad)) && setVerticalAngle((float)Math.toDegrees(verticalRad));

        if(!result)
        {
            trackingMap.remove(trackingId);
            setHorizontalAngle(0);
            setVerticalAngle(0);
        }
    }
}
