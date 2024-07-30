package aircraftsimulator.GameObject.Aircraft.Radar.Wave;

import aircraftsimulator.Game;
import aircraftsimulator.GameObject.Aircraft.Communication.Data.Data;

import javax.vecmath.Vector3f;

public class ElectroMagneticWaveData implements Data {
    private final Vector3f position;
    private final float created;

    public ElectroMagneticWaveData(ElectroMagneticWave wave)
    {
        position = wave.getPosition();
        created = Game.getGameTime();
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getCreated()
    {
        return created;
    }
}
