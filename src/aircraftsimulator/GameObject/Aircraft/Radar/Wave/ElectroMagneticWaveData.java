package aircraftsimulator.GameObject.Aircraft.Radar.Wave;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.Data;

import javax.vecmath.Vector3f;

public class ElectroMagneticWaveData implements Data {
    private final Vector3f position;

    public ElectroMagneticWaveData(ElectroMagneticWave wave)
    {
        position = wave.getPosition();
    }

    public Vector3f getPosition() {
        return position;
    }
}
