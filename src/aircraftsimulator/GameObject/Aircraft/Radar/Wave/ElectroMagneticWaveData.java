package aircraftsimulator.GameObject.Aircraft.Radar.Wave;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.Data;

import javax.vecmath.Vector3f;

public class ElectroMagneticWaveData implements Data {
    private final Vector3f position;
    private final Object data;

    public ElectroMagneticWaveData(ElectroMagneticWave wave, String code)
    {
        position = wave.getPosition();
        data = wave.getData(code);
    }

    public Vector3f getPosition() {
        return position;
    }

    public Object getData() {
        return data;
    }
}
