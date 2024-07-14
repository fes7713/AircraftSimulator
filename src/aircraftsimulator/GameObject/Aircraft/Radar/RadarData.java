package aircraftsimulator.GameObject.Aircraft.Radar;

import aircraftsimulator.GameObject.Aircraft.Communication.Data.Data;
import aircraftsimulator.GameObject.Aircraft.Radar.Wave.ElectroMagneticWaveData;

import java.util.List;

public record RadarData(List<ElectroMagneticWaveData> waves) implements Data {
}
