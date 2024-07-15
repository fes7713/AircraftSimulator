package aircraftsimulator.GameObject.Aircraft.Radar.Radar;

import aircraftsimulator.Environment;
import aircraftsimulator.GameObject.Aircraft.CentralStrategy.CommunicationData;
import aircraftsimulator.GameObject.Aircraft.CentralStrategy.DirectionalCommunicationData;
import aircraftsimulator.GameObject.Aircraft.Communication.Logger.Logger;
import aircraftsimulator.GameObject.Aircraft.Communication.Network;
import aircraftsimulator.GameObject.Aircraft.Communication.NetworkComponent;
import aircraftsimulator.GameObject.Aircraft.Communication.SlowStartApplicationNetworkComponentImp;
import aircraftsimulator.GameObject.Aircraft.MovingObjectInterface;
import aircraftsimulator.GameObject.Aircraft.Radar.Wave.ElectroMagneticWave;
import aircraftsimulator.GameObject.Aircraft.SystemPort;
import aircraftsimulator.GameObject.GameObject;
import aircraftsimulator.PaintDrawer;

import javax.vecmath.Vector3f;
import java.awt.*;
import java.util.List;

public class RadioCommunicator extends RadioEmitter implements RadarInterface{
    protected final NetworkComponent networkComponent;

    public RadioCommunicator(GameObject parent, Network network, float frequency, float power, float antennaDiameter, float detectionSNR) {
        super(parent, network, frequency, power, antennaDiameter, detectionSNR);
        networkComponent = new SlowStartApplicationNetworkComponentImp(network);

        networkComponent.openPort(SystemPort.COMMUNICATION);
        networkComponent.enabledPortTransfer(SystemPort.COMMUNICATION);

        networkComponent.addDataReceiver(CommunicationData.class, (data, port) -> {
            Environment.getInstance().addPulseWave(new ElectroMagneticWave(parent, parent.getPosition(), power * gain, frequency, ((MovingObjectInterface)parent).getDirection(), angle, data));
            Logger.Log(Logger.LogLevel.INFO, "Wireless communication [%s]".formatted(data), networkComponent.getMac(), 0);
        });
        networkComponent.addDataReceiver(DirectionalCommunicationData.class, (data, port) -> {
            Vector3f direction = new Vector3f(data.getTarget());
            direction.sub(data.getSource());
            Environment.getInstance().addPulseWave(new ElectroMagneticWave(parent, parent.getPosition(), power * gain, frequency, direction, angle, data.communicationData()));
            Logger.Log(Logger.LogLevel.INFO, "Wireless communication [%s]".formatted(data), networkComponent.getMac(), 0);
        });
    }

    protected void detect() {
        Environment environment = Environment.getInstance();
        List<ElectroMagneticWave> posList = environment.detectWave(parent);
        for(ElectroMagneticWave wave: posList)
            if(wave.getIntensity() > minimumDetectionIntensity && wave.getFrequency() == frequency && wave.getData() != null)
            {
                networkComponent.sendData(SystemPort.COMMUNICATION, wave.getData().getData());
                Logger.Log(Logger.LogLevel.INFO, "Data received [%s]".formatted(wave.getData().getData()), networkComponent.getMac(), SystemPort.COMMUNICATION);
            }
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(PaintDrawer.opacColor(color, 0.3F));
        Vector3f position = parent.getPosition();
        g2d.drawOval((int)(position.x - maxWaveRange), (int)(position.y - maxWaveRange), (int)(maxWaveRange * 2), (int)(maxWaveRange * 2));
        g2d.drawOval((int)(position.x - maxDetectionRange), (int)(position.y - maxDetectionRange), (int)(maxDetectionRange * 2), (int)(maxDetectionRange * 2));
    }
}
