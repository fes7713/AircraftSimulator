package aircraftsimulator.GameObject.Aircraft.Radar.Radar;

import aircraftsimulator.Environment;
import aircraftsimulator.GameObject.Aircraft.Communication.Network;
import aircraftsimulator.GameObject.Aircraft.Communication.NetworkComponent;
import aircraftsimulator.GameObject.Aircraft.Communication.SlowStartApplicationNetworkComponentImp;
import aircraftsimulator.GameObject.Aircraft.Radar.RadarData;
import aircraftsimulator.GameObject.Aircraft.Radar.Wave.ElectroMagneticWave;
import aircraftsimulator.GameObject.Aircraft.Radar.Wave.ElectroMagneticWaveData;
import aircraftsimulator.GameObject.Aircraft.SystemPort;
import aircraftsimulator.GameObject.GameObject;

import java.util.ArrayList;
import java.util.List;

public class AngleRadar extends RadioEmitter implements RadarInterface{
    protected final NetworkComponent networkComponent;

    protected final long interval;

    protected RadarMode mode;

    public AngleRadar(GameObject parent, Network network, float frequency, long interval, float power, float radarMaxAngle, float antennaDiameter, float detectionSNR) {
        super(parent, network, frequency, power, radarMaxAngle, antennaDiameter, detectionSNR);

        this.interval = interval;
        networkComponent = new SlowStartApplicationNetworkComponentImp(network){
            @Override
            public void handleConnectionEstablished(String sessionId, Integer port) {
                super.handleConnectionEstablished(sessionId, port);
                networkComponent.registerTimeout(SystemPort.SEARCH_RADAR, 1000, p -> {
                    if(!setHorizontalAngle(radarHorizontalAngle - beamAngle))
                    {
                        setHorizontalAngle(360);
                        System.out.println(radarVerticalAngle);
                        if(!setVerticalAngle(radarVerticalAngle - beamAngle))
                            setVerticalAngle(360);
                    }
                    illuminate();
                    networkComponent.updateTimeout(p, interval);
                });
            }
        };
        networkComponent.openPort(SystemPort.SEARCH_RADAR);
        networkComponent.enabledPortTransfer(SystemPort.SEARCH_RADAR);

        System.out.println("[%s] %s:%f, %s:%f".formatted("AngleRadar", "Angle", beamAngle, "Gain", gain) );
    }

    protected void detect() {
        Environment environment = Environment.getInstance();
        List<ElectroMagneticWave> posList = environment.detectWave(parent);
        List<ElectroMagneticWaveData> detectedPosList = new ArrayList<>();
        for(ElectroMagneticWave wave: posList)
            if(wave.getIntensity() > minimumDetectionIntensity && wave.getFrequency() == frequency)
            {
                detectedPosList.add(new ElectroMagneticWaveData(wave));
            }
        if(!detectedPosList.isEmpty())
        {
            networkComponent.sendData(SystemPort.SEARCH_RADAR, new RadarData(detectedPosList));
        }
    }
}
